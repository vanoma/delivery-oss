# delivery-api

## Installing dependencies

You must install `mysql-client` before you can install all pytohn dependencies successfully.
1. Run `brew install mysql-client`
2. Modify `.zshrc` or `.bash_profile` (depending on which shell you use) to add `mysql-client` to `PATH`: `export PATH="/usr/local/opt/mysql-client/bin:$PATH"`
3. Finally install dependencies `poetry install`

### Task queue

We are using [huey](https://github.com/coleifer/huey) given how it's lightweight unlike [celery](https://docs.celeryq.dev/en/stable/index.html) which is more heavy weight. Other task queues considered include [dramatiq](https://github.com/Bogdanp/dramatiq) which requires a separate broker (e.g. Redis) that we are not planning to have right same as [django-q](https://django-q.readthedocs.io/en/latest/). Huey is super minimal; in fact it has an extension called [Mini-Huey](https://huey.readthedocs.io/en/latest/contrib.html#mini-huey) which allows to have background tasks that run in the same server process like [FastAPI](https://fastapi.tiangolo.com/tutorial/background-tasks/); very superb!

## Running tests

Run `poetry shell` followed by `SECRET_KEY=me ./manage.py test`. Be sure to set required environment variables first.

## Starting a development server

Run `bash docker.sh`

## Creating git commits

We use `pre-commit` hooks to reformat the code before each commit. Once you have installed python dependencies, run the following commands to avoid getting issues when committing.
1. Open a poetry shell: `poetry shell`
2. Install `pre-commit` hooks: `pre-commit install`

Note: you must be inside poetry shell to run `git commit ...`. Otherwise, `pre-commit` command may not be available.
