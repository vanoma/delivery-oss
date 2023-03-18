from huey import MemoryHuey  # type: ignore
from huey.contrib.mini import MiniHuey  # type: ignore
from vanoma_api_utils.misc import resolve_environment

if resolve_environment() == "testing":
    # Use in-memory huey so we can execute tasks immediatary during tests.
    mini_huey = MemoryHuey(immediate=True)
else:
    mini_huey = MiniHuey()
