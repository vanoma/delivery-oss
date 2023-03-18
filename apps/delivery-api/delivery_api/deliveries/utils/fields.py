import io
import logging
from typing import Optional
from mutagen.mp3 import MP3  # type: ignore
from drf_extra_fields.fields import Base64FileField  # type: ignore


class MP3Base64FileField(Base64FileField):
    ALLOWED_TYPES = ["mp3"]

    def get_file_extension(self, filename: str, decoded_file: bytes) -> Optional[str]:
        try:
            MP3(io.BytesIO(decoded_file))
            return "mp3"
        except Exception as exc:
            logging.exception(str(exc))
            return None
