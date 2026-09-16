# Lokalna slikovna faza

Datum: 2026-09-16.

U aplikaciju su dodane dvije lokalne generirane kategorijske ilustracije:

| Resurs | Namjena | SHA-256 |
|---|---|---|
| `/images/vehicles/generic-vehicle.jpg` | Neutralni fallback za katalog bez odobrene točne fotografije | `73faeb75a17cf529352703a68f32b197966c42e368129c694834019e01526e06` |
| `/images/vehicles/generic-electric.jpg` | Neutralni fallback za čisti `Electric` zapis bez odobrene točne fotografije | `3fb2b1c0afe2d6a37e79b05dcc445a42c1e4b3a29df657093aa143e1c4c2b884` |

Obje slike su generirane ugrađenim Codex image-generation alatom, zatim obrađene kroz postojeći lokalni pipeline. JPEG su, dimenzija 900×600, bez runtime mrežnog poziva. Podrijetlo je zapakirano u `src/main/resources/images/vehicle-image-provenance.csv` i `.html`.

Ilustracije su namjerno neutralne i bez logotipa. Ne tvrde da prikazuju točnu marku, model, generaciju ili VIN podudaranje. UI ih koristi samo kada je `image_path` neutralni fallback; buduća model-specific fotografija s ručno provjerenom generacijom i licencom može je zamijeniti kroz postojeći `import-images` workflow.

`VehicleImageTest` provjerava classpath učitavanje svih dviju ilustracija i izvornog geometrijskog fallbacka, uključujući čisti offline rad. Stvarni Wikimedia/Commons lookup, ručni pregled kandidata i model-specific licence nisu automatski odobreni i ostaju zasebna opcionalna faza.
