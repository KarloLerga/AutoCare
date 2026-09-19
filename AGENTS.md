# AutoCare - finalne upute za Codex / AI integratora

Ovaj projekt je već arhitekturno odlučen. Ne vraćaj staru automatsku dijagnostiku i ne izmišljaj novi sloj ili framework.

## Autoritativni model

Aplikacija je za **vlasnika vozila**, ne za servis.

Tri odvojene funkcije:
1. Bilješke - slobodan tekst + gruba kategorija, bez dijagnoze.
2. Katalog - searchable informativni rasponi cijena standardnih zahvata.
3. Servisi - stvarno napravljeni radovi i stvarno plaćene cijene nakon mehaničara.

Ne vraćati `DiagnosticRule`, keyword scoring, match %, suggested repair na Problem ili automatsko pogađanje kvara.

## Kod

- Java 25, Swing/FlatLaf, Maven, JPA `EntityManager`, Hibernate, Azure SQL Server.
- Bez Springa, Lomboka, DI frameworka, MapStructa, runtime AI-ja i image API-ja.
- `src/main/java` mora ostati studentski čitljiv: bez lambda izraza, Stream API-ja, Optionala, `java.util.function`, `var`, recorda i generičkih callback frameworka.
- Koristi obične `for` petlje, `if/else`, klasične anonimne `ActionListener` klase i eksplicitne transakcije.
- View prikazuje Swing; Controller obrađuje GUI događaj; Service radi poslovna pravila/transakciju; Repository s postojećim EntityManagerom radi JPA upit.
- Ne spajati Service/Repository/View samo radi kraćeg broja datoteka.

## Finalni podaci

- 30.366 `VehicleVariant`
- 120 `WorkDefinition`
- 30 MAINTENANCE + 90 REPAIR
- 5 `VehiclePriceClass`
- 600 `WorkPriceRange`
- nema runtime per-variant `VehicleWorkRule` matrice
- maintenance interval je na `WorkDefinition`
- Katalog nema runtime fallback cijenu; svih 600 raspona mora postojati u bazi

## Baza

Autoritativna migracija je `schema/11_professor_model.sql`, audit `schema/12_professor_model_audit.sql`.
Ne pokretati stare 01-10 migracije niti stare complete-catalog importere.

Na pravom razvojnom računalu:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Complete-Setup.ps1 `
  -ConfigPath .\connection.local.json `
  -ApplyProfessorModel
```

Nakon toga `final_error_count` iz audita mora biti 0.

Nikada ne tvrdi da je Azure migracija ili GUI PASS ako stvarno nije pokrenuta. Ne commitati lozinke ili connection.local.json.
