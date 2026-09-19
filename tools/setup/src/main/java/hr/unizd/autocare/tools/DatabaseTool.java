package hr.unizd.autocare.tools;

/** Ulazna točka za developerske SQL naredbe; GUI je nikad ne poziva. */
public final class DatabaseTool {
  private DatabaseTool() {}

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Naredbe: sql-check, db-list, apply-final-schema, final-audit");
      return;
    }
    SqlSeedTool.run(args);
  }
}
