FROM public.ecr.aws/z6e0s8r6/docker-image-python

# Create working dir
RUN mkdir /code
WORKDIR /code

# Install dependencies
ADD pyproject.toml /code
ADD poetry.lock /code
RUN poetry install --no-root --no-interaction

# Copy code & run tests
COPY . /code
RUN ENVIRONMENT=testing SECRET_KEY=<FILL_IN_VALUE> GOOGLE_MAPS_API_KEY=<FILL_IN_VALUE> python manage.py test
