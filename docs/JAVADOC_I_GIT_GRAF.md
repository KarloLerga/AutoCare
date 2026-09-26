# Javadoc i Git graf

## Javadoc

Javadoc se generira iz završnog sourcea projekta.

Postupak:

1. Pregledati Java klase koje čine javni API aplikacije.
2. Dokumentirati javne klase i važnije javne/protected konstruktore i metode.
3. Posebno dokumentirati `Main`, `DatabaseConfig`, Controller, Service, Repository, Domain, `Session`, Observer i Strategy dijelove.
4. Za trivijalne gettere i settere nisu potrebni nepotrebno dugi komentari.
5. Pokrenuti `.\mvnw.cmd --no-transfer-progress javadoc:javadoc`.
6. Provjeriti da generiranje završava bez Javadoc grešaka.
7. Maven generira HTML u `target/reports/apidocs/`; kopirati taj output u `javadoc/` kako bi API dokumentacija bila priložena uz projekt.
8. Početna stranica priložene dokumentacije je `javadoc/index.html`.

## Git aciklički graf

Git graf generira se iz stvarne povijesti repozitorija nakon završnog commita dokumentacije i Javadoca.

Naredba:

```bash
git log --graph --oneline --decorate --all --date-order > git/git_graph.txt
```

Tekstualni output sprema se kao `git/git_graph.txt` nakon dokumentacijskog commita. Uz njega se može priložiti čitljivi PNG/SVG prikaz istog grafa. Povijest se ne rekonstruira ručno.
