import csv, hashlib, importlib.util, io, json, sys, tempfile, unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/'scripts'))
import build_af3 as b
IMAGES=ROOT.parent/'images/enrich_images.py'
spec=importlib.util.spec_from_file_location('image_tool',IMAGES);image=importlib.util.module_from_spec(spec);spec.loader.exec_module(image)

def read_csv(path):
 with path.open(encoding='utf-8',newline='') as handle:return list(csv.DictReader(handle))

class EnrichmentChecks(unittest.TestCase):
 @classmethod
 def setUpClass(cls):
  cls.works={w['code']:w for w in json.loads((ROOT/'config/works.json').read_text(encoding='utf-8'))}
  cls.traits=[b.load_traits(r) for r in read_csv(ROOT/'base-input/vehicle_traits.csv')]
  cls.ev=next(t for t in cls.traits if t['make']=='Tesla' and t['generation']=='Model 3 (2023)')
  cls.diesel=next(t for t in cls.traits if t['make']=='Renault' and t['fuel_class']=='DIESEL' and t['year_from']==2015)
 def test_catalog_size(self):self.assertEqual(30366,len(self.traits))
 def test_unique_codes(self):self.assertEqual(len(self.traits),len({t['variant_code'] for t in self.traits}))
 def test_works_unique(self):self.assertEqual(122,len(self.works))
 def test_ev_no_oil(self):self.assertEqual('NOT_APPLICABLE',b.decision(self.ev,self.works['OIL_SERVICE'])[0])
 def test_ev_no_glowplugs(self):self.assertEqual('NOT_APPLICABLE',b.decision(self.ev,self.works['GLOW_PLUGS'])[0])
 def test_ev_no_timing(self):self.assertEqual('NOT_APPLICABLE',b.decision(self.ev,self.works['TIMING_BELT_PUMP'])[0])
 def test_battery_no_arbitrary_quote(self):self.assertEqual('QUOTE_REQUIRED',b.decision(self.ev,self.works['HV_BATTERY_REPLACE'])[0])
 def test_diesel_no_hv_pack(self):self.assertEqual('NOT_APPLICABLE',b.decision(self.diesel,self.works['HV_BATTERY_REPLACE'])[0])
 def test_manual_category_not_recommendation(self):self.assertEqual('NOT_APPLICABLE',b.decision(self.ev,self.works['OTHER_REPAIR'])[0])
 def test_tesla_fluid_not_fixed_replacement(self):self.assertEqual('CONDITION_BASED',b.schedule_kind(self.ev,self.works['BRAKE_FLUID']))
 def test_tesla_brake_check_source(self):
  refs={r['work_code']:r for r in b.referenced(self.ev)}
  self.assertEqual(48,refs['BRAKE_FLUID_CHECK']['interval_months']);self.assertNotIn('BRAKE_FLUID',refs)
 def test_tesla_modely_year_ambiguous_is_review_only(self):
  t=next(t for t in self.traits if t['make']=='Tesla' and t['generation']=='Model Y (2019)')
  self.assertFalse(b.referenced(t));self.assertEqual(3,len(b.market_candidates(t)))
 def test_kia_not_silently_hr(self):
  t=next(t for t in self.traits if t['make']=='Kia' and t['model']=='EV6')
  self.assertFalse(b.referenced(t));self.assertTrue(all(r['review_status'].startswith('DRAFT') for r in b.market_candidates(t)))
 def test_model_y_no_bad_reference(self):self.assertFalse(any('2020_2024_modely' in r['interval_source'] for r in read_csv(ROOT/'data/referenced_intervals.csv')))
 def test_overlaps_not_unrelated(self):
  m=json.loads((ROOT/'config/model.json').read_text(encoding='utf-8'));self.assertNotIn(['WHEEL_ALIGNMENT','SCHEDULED_INSPECTION'],m['overlap_pairs'])
 def test_data_checksums(self):
  for line in (ROOT/'data/checksums.sha256').read_text(encoding='utf-8').splitlines():
   sha,name=line.split('  ',1);self.assertEqual(sha,hashlib.sha256((ROOT/'data'/name).read_bytes()).hexdigest())
 def test_sample_integrity(self):
  for line in (ROOT/'data/sample/checksums.sha256').read_text(encoding='utf-8').splitlines():
   sha,name=line.split('  ',1);self.assertEqual(sha,hashlib.sha256((ROOT/'data/sample'/name).read_bytes()).hexdigest())

class ImageChecks(unittest.TestCase):
 def test_http_blocked(self):
  with self.assertRaises(ValueError):image.safe_url('http://upload.wikimedia.org/a.jpg')
 def test_external_host_blocked(self):
  with self.assertRaises(ValueError):image.safe_url('https://example.com/a.jpg')
 def test_suffix_attack_blocked(self):
  with self.assertRaises(ValueError):image.safe_url('https://upload.wikimedia.org.attacker.example/a.jpg')
 def test_url_credentials_blocked(self):
  with self.assertRaises(ValueError):image.safe_url('https://a:b@upload.wikimedia.org/a.jpg')
 def test_port_blocked(self):
  with self.assertRaises(ValueError):image.safe_url('https://upload.wikimedia.org:8080/a.jpg')
 def test_good_url(self):self.assertTrue(image.safe_url('https://upload.wikimedia.org/x.jpg'))
 def test_unapproved_skipped(self):self.assertFalse(image.validate_review({'status':'DRAFT'}))
 def test_missing_review_not_promoted(self):
  with self.assertRaises(ValueError):image.validate_review({'status':'APPROVED'})
 def test_unknown_license_blocked(self):
  with self.assertRaises(ValueError):image.license_info({'extmetadata':{}})
 def test_supported_license(self):
  m={'LicenseShortName':{'value':'CC BY-SA 4.0'},'LicenseUrl':{'value':'https://creativecommons.org/licenses/by-sa/4.0/'},'Artist':{'value':'<a>Photographer</a>'}}
  self.assertEqual('Photographer',image.license_info({'extmetadata':m})[2])
 def test_html_stripped(self):self.assertEqual('A & B',image.plain('<b>A &amp; B</b>'))
 def test_contact_required(self):
  with tempfile.TemporaryDirectory() as td:
   with self.assertRaises(ValueError):image.Client('anonymous',Path(td))
 def test_resize(self):
  try:from PIL import Image
  except ImportError:self.skipTest('Pillow not installed; install tools/images/requirements.txt locally.')
  raw=io.BytesIO();Image.new('RGB',(1600,900)).save(raw,format='PNG')
  out=image.process_image(raw.getvalue());im=Image.open(io.BytesIO(out))
  self.assertEqual((960,540),im.size);self.assertEqual('JPEG',im.format)
 def test_group_membership_complete(self):
  groups={r['group_id'] for r in image.rows(ROOT/'data/image_groups.csv')};members=image.rows(ROOT/'data/image_group_variants.csv')
  self.assertEqual(30366,len(members));self.assertTrue(all(r['group_id'] in groups for r in members))

if __name__=='__main__':unittest.main()
