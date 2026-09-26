# Dijagrami

Tekstualni ERD i UML izvori izrađeni su u **Mermaid notaciji**. Završne PNG slike iz priloženih materijala nalaze se uz izvore i nisu redizajnirane.

## ERD

- Mermaid izvor: `ERD_AutoCare_FINAL.mmd`
- PNG izvoz: `ERD_AutoCare_FINAL.png`

ERD prikazuje persistentni model AutoCare aplikacije. Posebno su prikazane dvije različite veze između korisnika i vozila:

- `owns` - vlasništvo nad vozilima preko `VEHICLE.owner_id`
- `activeVehicle` - trenutno aktivno vozilo preko `APP_USER.active_vehicle_id`

## UML

- Mermaid izvor: `UML_AutoCare_FINAL.mmd`
- Završna PNG slika: `UML_AutoCare_FINAL.png`
- Markdown za pregled i copy/paste: `UML_AutoCare_FINAL.md`

UML je organiziran prema stvarnim paketima izvornog koda:

- `app`
- `view`
- `controller`
- `service`
- `repository`
- `observer`
- `domain`
- `strategy`

Dijagram prikazuje glavne klase, važne atribute, konstruktore, glavne metode i ključne ovisnosti. Glavni tok prati smjer Controller -> Service -> Repository, dok su Observer i Strategy dijelovi izdvojeni u svojim stvarnim paketima.

Radi čitljivosti ne crta se dodatna strelica za svaku ovisnost koja je već jasno vidljiva iz atributa klase. Grafičke veze koriste se za glavne odnose između slojeva, kompoziciju Viewova, Observer komunikaciju i Strategy strukturu. Time se smanjuje križanje linija bez gubitka važnih informacija o strukturi aplikacije.

Persistentni entiteti detaljno su prikazani ERD-om pa se na aplikacijskom UML-u ne dupliciraju kao pune klase. Pomoćni GUI, utility i podatkovni tipovi opisani su u `docs/UML_OPIS.md`.

Za diagrams.net/draw.io može se koristiti `Arrange -> Insert -> Mermaid` i zalijepiti sadržaj `.mmd` datoteke. Nakon umetanja dopušteno je samo ručno razmaknuti elemente i prilagoditi putanju konektora radi čitljivosti; time se ne mijenja semantika UML-a.

## GUI wireframeovi

GUI wireframeovi izrađeni su u **Figmi** i nalaze se u `wireframes/AutoCare_GUI_Wireframes_FINAL.pdf`.
