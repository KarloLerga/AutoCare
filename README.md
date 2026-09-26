# AutoCare

AutoCare je Java Swing aplikacija za vođenje vozila, servisne povijesti, stvarnih troškova servisa, korisnički prijavljenih problema i planiranog održavanja. Podaci se pohranjuju u Azure SQL bazu.

## Pokretanje

Potreban je JDK 25. Projekt se gradi Maven Wrapperom:

```powershell
.\mvnw.cmd --no-transfer-progress clean package
```

Nakon uspješnog builda aplikacija se pokreće naredbom:

```powershell
java -jar target/autocare-1.0.0.jar
```

Ulazna klasa je `hr.unizd.autocare.app.Main`. Za pokretanje aplikacije potrebna je dostupna Azure SQL baza.

## Projektna dokumentacija

- [Završna dokumentacija](docs/AutoCare_Zavrsna_Dokumentacija.md)
- [UML dijagram i opis](diagrams/UML_AutoCare_FINAL.png) · [Mermaid izvor](diagrams/UML_AutoCare_FINAL.mmd) · [objašnjenje](docs/UML_OPIS.md)
- [ERD dijagram i opis](diagrams/ERD_AutoCare_FINAL.png) · [Mermaid izvor](diagrams/ERD_AutoCare_FINAL.mmd) · [objašnjenje](docs/ERD_OPIS.md)
- [Figma GUI wireframeovi](wireframes/AutoCare_GUI_Wireframes_FINAL.pdf) · [opis](docs/GUI_WIREFRAME_OPIS.md)
- [Javadoc i Git graf](docs/JAVADOC_I_GIT_GRAF.md)
- [Vanjske biblioteke](docs/VANJSKE_BIBLIOTEKE.md)
- [Checklist predaje](docs/CHECKLIST_PREDAJE.md)

UML i ERD PNG datoteke pohranjene su kao priložene. Mermaid izvori omogućuju čitanje i uređivanje tekstualnog modela; priložene slike nisu ponovno nacrtane.
