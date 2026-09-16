"""Compatibility entrypoint for the active AF3 structural validator."""
import runpy
from pathlib import Path
runpy.run_path(str(Path(__file__).with_name("validate_af3.py")),run_name="__main__")
