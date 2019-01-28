# https://github.com/elastic/kibana-docker
FROM docker.elastic.co/kibana/kibana:6.4.2

# Add your kibana plugins setup here
# Example: RUN kibana-plugin install <name|url>
ADD config/kibana.yml /usr/share/kibana/config

#USER root && RUN chown kibana:kibana /usr/share/kibana/config/kibana.yml && USER kibana && EXPOSE 5601

