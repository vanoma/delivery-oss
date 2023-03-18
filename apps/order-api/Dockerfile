FROM public.ecr.aws/z6e0s8r6/docker-image-java

# Create working dir
RUN mkdir /code
WORKDIR /code

# Install dependencies
ADD pom.xml /code
RUN ["/usr/local/bin/mvn-entrypoint.sh", "mvn", "verify", "clean", "--fail-never"]

# Build application
ADD . /code
RUN mvn package -Dmaven.test.skip # TODO: Skipping tests until we find a solution for @MockBean - https://tinyurl.com/yc2hcv2w
