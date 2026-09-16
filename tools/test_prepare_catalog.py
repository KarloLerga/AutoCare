import csv,importlib.util,json,tempfile,unittest
from pathlib import Path
spec=importlib.util.spec_from_file_location("prepare",Path(__file__).with_name("prepare_catalog.py"))
prepare=importlib.util.module_from_spec(spec);spec.loader.exec_module(prepare)
class CatalogueTests(unittest.TestCase):
 def row(self):
  row=dict.fromkeys(prepare.REQUIRED,"");row.update(make="DEMO",model="A",generation="G",gen_year_start="2010",gen_year_end="2020",engine_label="D",power_hp="90",fuel_type="Diesel");return row
 def test_normalization_is_stable(self):
  a=prepare.normalize(self.row());b=prepare.normalize(self.row());self.assertEqual(a["code"],b["code"]);self.assertEqual(90,a["power_hp"])
 def test_different_specs_remain_distinct(self):
  a=self.row();b=self.row();b["transmission"]="automatic";self.assertNotEqual(prepare.normalize(a)["code"],prepare.normalize(b)["code"])
 def test_invalid_rows_are_not_guessed(self):
  for field,value in [("gen_year_start",""),("gen_year_end","2000"),("power_hp","90.5"),("make","")]:
   row=self.row();row[field]=value
   with self.assertRaises(ValueError):prepare.normalize(row)
 def test_counts_rejections_and_duplicates(self):
  with tempfile.TemporaryDirectory() as tmp:
   path=Path(tmp)/"source.csv";rows=[self.row(),self.row(),self.row()];rows[-1]["gen_year_start"]="missing"
   with path.open("w",newline="",encoding="utf-8") as f:
    writer=csv.DictWriter(f,fieldnames=prepare.REQUIRED);writer.writeheader();writer.writerows(rows)
   manifest=prepare.prepare(path,Path(tmp)/"out");self.assertEqual({"input_rows":3,"accepted_rows":1,"exact_duplicates":1,"rejected_rows":1},manifest["counts"])
if __name__=="__main__":unittest.main()
