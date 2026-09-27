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

## Dokumentacija

- [Završna projektna dokumentacija](DOKUMENTACIJA.md)
- [**Otvori Javadoc u pregledniku ↗**](https://karlolerga.github.io/AutoCare/)

Javadoc se automatski generira i objavljuje na GitHub Pages pri promjenama na grani `main`. Lokalno se može ponovno generirati naredbom `.\mvnw.cmd javadoc:javadoc`.
