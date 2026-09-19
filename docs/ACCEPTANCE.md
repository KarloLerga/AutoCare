# Završni kriteriji prihvata

Prije predaje svaki redak mora imati stvarni PASS ili pošteni NOT_RUN/BLOCKED.

- JDK 25 i `mvnw.cmd clean verify` prolaze.
- `mvnw.cmd javadoc:javadoc` prolazi.
- `python scripts/validate_professor_catalog.py` vraća `errors = 0`.
- Runtime JAR ne sadrži privatne credentiale.
- Azure SQL `schema/11_professor_model.sql` je primijenjen na točnu bazu.
- `schema/12_professor_model_audit.sql` završava s `final_error_count = 0`.
- GUI ručno prolazi registraciju/login, vozila, Bilješke, Katalog, Servise, Održavanje i Dashboard.
- Informativna cijena iz Kataloga se nikad ne sprema kao stvarno plaćena cijena.
- Novi servis ispravno ažurira kilometražu i sljedeći maintenance interval.
- Aktivna bilješka se može ručno zatvoriti ili riješiti stvarnim servisom.
- Git povijest sadrži stvarne smislene commitove, bez izmišljanja/backdateanja.
