# Vehicle Image Enrichment - LAST development phase
Application runtime uses only local classpath resources. No external API in Swing/Service, no BLOB, no API dependency for users. `VehicleVariant.imagePath` is the only persistence field needed. One group is make + model + generation; bodyType may be refined manually. A representative year is lookup context, NOT proof of generation.

## Why direct Wikidata/Commons
trustcarinfo/carapi was considered. Direct public APIs avoid hosting/running an extra PHP service and expose candidate IDs and licences for review. Neither fuzzy text search nor P18 nor a photo date proves the exact generation. The pipeline NEVER automatically turns a candidate into APPROVED.

## Files / commands (project root)
Python3.11+, optional virtual environment; install `tools/images/requirements.txt` (Pillow12.3.0). Core app/data import does not need Python.
```powershell
py -3 -m venv .image-venv
.\.image-venv\Scripts\python.exe -m pip install -r tools/images/requirements.txt
.\.image-venv\Scripts\python.exe tools/images/enrich_images.py --help
.\.image-venv\Scripts\python.exe tools/images/enrich_images.py plan
$env:AUTOCARE_IMAGE_USER_AGENT='AutoCareStudent/1.0 (contact: YOUR_REAL_CONTACT)'
.\.image-venv\Scripts\python.exe tools/images/enrich_images.py lookup --limit 20 --offset 0
```
Read CLI help for actual paths/options before overriding. The plan already groups6.697generation records. Lookup is bounded (default20, at most200 per call), caches successful responses and respects rate/backoff. Do not crawl thousands of requests at once or pretend all were reviewed. Resuming uses offset/cache and preserves user edits.

Review candidates in the generated CSV. Confirm actual make/model/GENERATION, not just a nice photo. Set status APPROVED, real reviewer, review_date, generation_evidence and license_reviewed YES only after checking. Correct Commons File title manually for a mismatch. Exclude unknown/ambiguous cases; fallback is a valid result.
```powershell
.\.image-venv\Scripts\python.exe tools/images/enrich_images.py download
# After approved files/credits exist in src/main/resources:
.\mvnw.cmd package
java -jar tools/setup/target/autocare-setup-1.0.0.jar import-images tools/images/approved_image_updates.csv
$env:AUTOCARE_SEED_TARGET=$env:AUTOCARE_DB_NAME
java -jar tools/setup/target/autocare-setup-1.0.0.jar import-images tools/images/approved_image_updates.csv --apply
```
The Java import reads classpath images from the rebuilt JAR and verifies SHA. It does not point the DB to an unbundled src/ file. `--replace-existing` is required before replacing a distinct already-approved image path. Normal rerun keeps good existing image bytes/hashes. A same file with changed source/title requires explicit reviewed replacement.

## Technical limits
HTTPS host allowlist, no URL credentials or unapproved redirects, max download10MB, max image20MP, JPEG/PNG/WEBP input, EXIF orientation, output bounded960x600 with preserved aspect ratio and JPEG quality85. No multi-megabyte original simply copied into resources. Filename is safe group-key-based, not uncontrolled upstream filename. No absolute file URL in imagePath. Existing fallback `/images/vehicles/fallback-car.jpg` is an original geometric placeholder, not a real vehicle photograph.

## Licence / audit
Commons imageinfo extmetadata contains HTML and must be parsed/escaped, not rendered blindly as remote active markup. Accept only the recognised PD/CC0/CC-BY/CC-BY-SA families and sufficient author/source/licence data. Other restrictions, missing metadata or unsupported format go to a failure/review report. Review is still required even when the short licence name is recognised.

Credits CSV/HTML are written with the packaged resources and include original Commons page/title, author, licence link, source, resizing note and local SHA. Preserve attribution and share-alike requirements for the derived image as applicable. Image licences are separate from vehicle-data ODbL and Java source. No assumption that every Wikimedia image has the same licence. Do not strip credit files to reduce package size.

No real network lookup/download/generation validation was executed in preparation. Pure safety/metadata/resize helper tests passed; these do not prove actual image matches. The normal application must remain functional even when this entire optional phase has no approved photo.
