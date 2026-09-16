import importlib.util,unittest
from pathlib import Path
from decimal import Decimal
s=importlib.util.spec_from_file_location("estimates",Path(__file__).with_name("calculate_estimates.py"));m=importlib.util.module_from_spec(s);s.loader.exec_module(m)
class EstimateTests(unittest.TestCase):
 def row(self):return dict(parts_low="70",parts_high="100",labour_hours_low="0.8",labour_hours_high="1.2",hourly_rate_gross="60",consumables_low="5",consumables_high="10",vat_basis="INCLUDED")
 def test_synthetic_arithmetic(self):self.assertEqual((Decimal("123.00"),Decimal("182.00"),Decimal("152.50")),m.estimate(self.row()))
 def test_vat_mix_rejected(self):
  r=self.row();r["vat_basis"]="EXCLUDED"
  with self.assertRaises(ValueError):m.estimate(r)
 def test_reversed_bounds_rejected(self):
  r=self.row();r["parts_low"]="200"
  with self.assertRaises(ValueError):m.estimate(r)
 def test_non_finite_rejected(self):
  r=self.row();r["hourly_rate_gross"]="NaN"
  with self.assertRaises(ValueError):m.estimate(r)
 def test_cost_approval_does_not_approve_unreviewed_interval(self):
  r=dict(review_status="APPROVED",reviewed_by="Reviewer",source_urls="https://example.com",checked_on="2026-09-15")
  self.assertFalse(m.approval(r,dict(review_status="DRAFT",interval_km="12345")))
  self.assertTrue(m.approval(r,dict(review_status="APPROVED")))
  self.assertTrue(m.approval(r,None))
if __name__=="__main__":unittest.main()
