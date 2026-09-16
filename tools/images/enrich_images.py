#!/usr/bin/env python3
"""Developer-only staged Wikimedia image enrichment. Nothing approves generation matches automatically."""
from __future__ import annotations
import argparse, csv, hashlib, html, io, json, os, re, time, warnings
from datetime import date
from pathlib import Path
from urllib.parse import urlencode, urlparse
from urllib.request import Request, build_opener, HTTPRedirectHandler
from urllib.error import HTTPError

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[1]
DATA = PROJECT / 'tools/reference-data/data'
FIELDS = ['group_id','make','model','generation','representative_year','qid','commons_title',
          'status','reviewer','review_date','generation_evidence','license_reviewed']
CREDIT_FIELDS = ['group_id','commons_title','image_path','artist','license','license_url','source_url',
                 'download_url','source_sha256','image_sha256','downloaded_on','changes','reviewer','generation_evidence']
ALLOWED_HOSTS={'www.wikidata.org','commons.wikimedia.org','upload.wikimedia.org','thumb.wikimedia.org'}

def rows(path: Path) -> list[dict[str,str]]:
    with path.open(encoding='utf-8-sig', newline='') as f: return list(csv.DictReader(f))

def write(path: Path, fields: list[str], records) -> None:
    path.parent.mkdir(parents=True,exist_ok=True)
    tmp=path.with_suffix(path.suffix+'.tmp')
    with tmp.open('w',encoding='utf-8',newline='') as f:
        w=csv.DictWriter(f,fields,extrasaction='ignore'); w.writeheader();w.writerows(records)
    tmp.replace(path)

def safe_url(url: str) -> str:
    p=urlparse(url)
    if p.scheme!='https' or p.hostname not in ALLOWED_HOSTS or p.username or p.password or p.port not in (None,443):
        raise ValueError('Only explicit HTTPS Wikimedia hosts are allowed.')
    return url

class SafeRedirect(HTTPRedirectHandler):
    def redirect_request(self,req,fp,code,msg,headers,newurl):
        return super().redirect_request(req,fp,code,msg,headers,safe_url(newurl))

class Client:
    def __init__(self,user_agent:str,cache:Path):
        if not user_agent or not ('@' in user_agent or 'https://' in user_agent):
            raise ValueError('Set AUTOCARE_IMAGE_USER_AGENT with contact, e.g. AutoCareSeed/1.0 (https://github.com/KarloLerga/AutoCare).')
        self.agent=user_agent;self.cache=cache;cache.mkdir(parents=True,exist_ok=True)
        self.opener=build_opener(SafeRedirect());self.last=0.0
    def fetch(self,url:str,max_bytes:int=10_000_000)->bytes:
        safe_url(url)
        for attempt in range(4):
            time.sleep(max(0,1.0-(time.monotonic()-self.last)))
            self.last=time.monotonic()
            try:
                with self.opener.open(Request(url,headers={'User-Agent':self.agent,'Accept':'application/json,image/jpeg,image/png,image/webp'}),timeout=35) as r:
                    safe_url(r.url)
                    content=r.read(max_bytes+1)
                    if len(content)>max_bytes:raise ValueError('Response exceeds size limit.')
                    return content
            except HTTPError as e:
                if e.code not in (429,503) or attempt==3:raise
                delay=e.headers.get('Retry-After','')
                time.sleep(min(120,int(delay) if delay.isdigit() else 5*(attempt+1)))
        raise RuntimeError('Download failed.')
    def api(self,endpoint:str,**params):
        params.update(format='json',maxlag=5)
        url=endpoint+'?'+urlencode(sorted(params.items()))
        path=self.cache/(hashlib.sha256(url.encode()).hexdigest()+'.json')
        if path.exists():return json.loads(path.read_text(encoding='utf-8'))
        raw=self.fetch(url,4_000_000);value=json.loads(raw)
        if 'error' in value:raise ValueError('API error: '+str(value['error'].get('code','unknown')))
        path.write_bytes(raw);return value

def lookup(client:Client,groups:list[dict],output:Path,limit:int,offset:int):
    existing=rows(output) if output.exists() else []
    done={r['group_id'] for r in existing}
    added=[]
    for g in groups[offset:offset+limit]:
        if g['group_id'] in done:continue
        # Year is only context; it is never a proof of generation identity.
        result=client.api('https://www.wikidata.org/w/api.php',action='wbsearchentities',search=g['make']+' '+g['model'],language='en',limit=5)
        candidates=[]
        for match in result.get('search',[]):
            qid=match['id']
            if not re.fullmatch(r'Q\d+',qid):continue
            entity=client.api('https://www.wikidata.org/w/api.php',action='wbgetentities',ids=qid,props='claims|labels|descriptions')['entities'][qid]
            for claim in entity.get('claims',{}).get('P18',[]):
                value=claim.get('mainsnak',{}).get('datavalue',{}).get('value')
                if isinstance(value,str):
                    candidates.append({**g,'qid':qid,'commons_title':'File:'+value,'status':'DRAFT','reviewer':'','review_date':'','generation_evidence':'','license_reviewed':''})
        if not candidates:candidates=[{**g,'status':'NO_MATCH'}]
        added.extend(candidates);done.add(g['group_id'])
        write(output,FIELDS,existing+added) # preserve partial progress and manual edits
    print(f'Lookup: {len(added)} candidates. All new rows require review.')

def plain(value:str)->str:
    return html.unescape(re.sub(r'<[^>]*>',' ',value)).strip()

def license_info(info:dict)->tuple[str,str,str]:
    meta=info.get('extmetadata',{})
    get=lambda k:plain(str(meta.get(k,{}).get('value','')))
    name=get('LicenseShortName');url=get('LicenseUrl');artist=get('Artist')
    if url.startswith('//'):url='https:'+url
    low=name.lower()
    # Explicit conservative allowlist. Do not infer public domain merely from absent metadata.
    allowed=(low in ('public domain','cc0','cc0 1.0') or re.fullmatch(r'cc by(?:-sa)? (?:1\.0|2\.0|2\.5|3\.0|4\.0)',low))
    if not allowed or (low!='public domain' and not url.startswith('https://')) or not artist:
        raise ValueError('License/artist needs manual handling; supported: explicit PD, CC0, CC BY, CC BY-SA.')
    return name,url,artist

def validate_review(r:dict):
    if r.get('status')!='APPROVED':return False
    for key in ('reviewer','review_date','generation_evidence','commons_title'):
        if not r.get(key,'').strip():raise ValueError(f'Approved row missing {key}.')
    date.fromisoformat(r['review_date'])
    if r.get('license_reviewed')!='YES':raise ValueError('license_reviewed must explicitly be YES.')
    if not re.fullmatch(r'img-[0-9a-f]{20}',r['group_id']):raise ValueError('Invalid group id.')
    if not r['commons_title'].startswith('File:'):raise ValueError('Use exact Commons File: title, not arbitrary URL.')
    return True

def process_image(raw:bytes)->bytes:
    from PIL import Image,ImageOps
    Image.MAX_IMAGE_PIXELS=20_000_000
    with warnings.catch_warnings():
        warnings.simplefilter('error',Image.DecompressionBombWarning)
        with Image.open(io.BytesIO(raw)) as source:
            if source.format not in ('JPEG','PNG','WEBP'):raise ValueError('Only raster photographs JPEG/PNG/WEBP supported.')
            source.load();im=ImageOps.exif_transpose(source).convert('RGB')
            im.thumbnail((960,600),Image.Resampling.LANCZOS)
            out=io.BytesIO();im.save(out,format='JPEG',quality=85,optimize=True)
            return out.getvalue()

def download(client:Client,review:Path,replace:bool=False):
    approved=[r for r in rows(review) if validate_review(r)]
    allowed={r['group_id']:r for r in rows(DATA/'image_groups.csv')}
    if len({r['group_id'] for r in approved})!=len(approved):raise ValueError('Only one APPROVED image per group.')
    credits_path=ROOT/'image_credits.csv'
    credits={r['group_id']:r for r in rows(credits_path)} if credits_path.exists() else {}
    resource=PROJECT/'src/main/resources/images/vehicles';resource.mkdir(parents=True,exist_ok=True)
    errors=[]
    for r in approved:
        gid=r['group_id']
        if gid not in allowed:raise ValueError('Unknown vehicle group: '+gid)
        output=resource/(gid+'.jpg');old=credits.get(gid)
        if old and output.exists() and old['commons_title']==r['commons_title'] and hashlib.sha256(output.read_bytes()).hexdigest()==old['image_sha256']:
            continue
        if output.exists() and not replace:raise ValueError('Existing image differs; inspect, then --replace-reviewed: '+gid)
        try:
            response=client.api('https://commons.wikimedia.org/w/api.php',action='query',titles=r['commons_title'],prop='imageinfo',
                iiprop='url|size|mime|extmetadata|timestamp',iiurlwidth=960,iilimit=1)
            info=next((p['imageinfo'][0] for p in response.get('query',{}).get('pages',{}).values() if p.get('imageinfo')),None)
            if info is None:raise ValueError('File not found.')
            if info.get('mime') not in ('image/jpeg','image/png','image/webp'):raise ValueError('Not a supported photograph.')
            lic,lic_url,artist=license_info(info)
            if info.get('extmetadata',{}).get('Restrictions',{}).get('value'):raise ValueError('Additional restrictions require manual legal review.')
            url=info.get('thumburl') or info['url'];raw=client.fetch(url)
            processed=process_image(raw);temp=output.with_suffix('.jpg.tmp');temp.write_bytes(processed);temp.replace(output)
            credits[gid]={'group_id':gid,'commons_title':r['commons_title'],'image_path':'/images/vehicles/'+output.name,
                'artist':artist,'license':lic,'license_url':lic_url,'source_url':info.get('descriptionurl',''),
                'download_url':url,'source_sha256':hashlib.sha256(raw).hexdigest(),'image_sha256':hashlib.sha256(processed).hexdigest(),
                'downloaded_on':date.today().isoformat(),'changes':'Resized <=960x600; orientation normalized; JPEG conversion; metadata removed.',
                'reviewer':r['reviewer'],'generation_evidence':r['generation_evidence']}
            write(credits_path,CREDIT_FIELDS,credits.values())
        except Exception as e:
            errors.append({'group_id':gid,'error':str(e)})
    write(ROOT/'download_errors.csv',['group_id','error'],errors)
    # Only verified files with an existing credit record are eligible for a database update.
    valid={g:c for g,c in credits.items() if (resource/(g+'.jpg')).exists() and hashlib.sha256((resource/(g+'.jpg')).read_bytes()).hexdigest()==c['image_sha256']}
    updates=[]
    for member in rows(DATA/'image_group_variants.csv'):
        c=valid.get(member['group_id'])
        if c:updates.append({'variant_code':member['variant_code'],'group_id':member['group_id'],'image_path':c['image_path'],'image_sha256':c['image_sha256']})
    write(ROOT/'approved_image_updates.csv',['variant_code','group_id','image_path','image_sha256'],updates)
    # Ship attribution inside the JAR and alongside the package; escaped text only, no remote elements.
    write(PROJECT/'src/main/resources/images/image_credits.csv',CREDIT_FIELDS,credits.values())
    body=''.join('<p>'+html.escape(' | '.join(c.get(k,'') for k in ('group_id','commons_title','artist','license','license_url','source_url','changes')))+'</p>' for c in credits.values())
    (PROJECT/'src/main/resources/images/image_credits.html').write_text('<!doctype html><meta charset="utf-8"><h1>Vehicle image credits</h1>'+body,encoding='utf-8')
    print(f'{len(valid)} verified local images, {len(updates)} variant mappings, {len(errors)} errors. Rebuild JAR before import-images.')
    if errors:raise SystemExit(2)

def main():
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('command',choices=['plan','lookup','download'])
    p.add_argument('--limit',type=int,default=20);p.add_argument('--offset',type=int,default=0)
    p.add_argument('--review',type=Path,default=ROOT/'review.csv');p.add_argument('--replace-reviewed',action='store_true')
    a=p.parse_args()
    if a.limit<1 or a.limit>200 or a.offset<0:p.error('Use controlled batches: limit 1..200, offset >=0.')
    groups=rows(DATA/'image_groups.csv')
    if a.command=='plan':
        write(ROOT/'plan.csv',list(groups[0]),groups)
        print(f'{len(groups)} groups. No web requests, downloads or database writes.');return
    client=Client(os.environ.get('AUTOCARE_IMAGE_USER_AGENT',''),ROOT/'.cache')
    if a.command=='lookup':lookup(client,groups,a.review,a.limit,a.offset)
    else:download(client,a.review,a.replace_reviewed)

if __name__=='__main__':main()
