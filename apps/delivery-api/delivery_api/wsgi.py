# Aapply gevent monkey-patch before anything else

from gevent import monkey  # type: ignore

monkey.patch_all()


# Start mini-huey

from delivery_api.huey import mini_huey

mini_huey.start()


# Django stuffs

import os
from django.core.wsgi import get_wsgi_application

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "delivery_api.settings")

application = get_wsgi_application()
