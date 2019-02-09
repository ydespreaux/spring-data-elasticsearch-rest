# https://github.com/elastic/elasticsearch-docker
FROM docker.elastic.co/elasticsearch/elasticsearch:6.2.4

# Add your elasticsearch plugins setup here
# Example: RUN elasticsearch-plugin install analysis-icu
ADD config/elasticsearch.yml /usr/share/elasticsearch/config/

#USER root && RUN chown elasticsearch:elasticsearch /usr/share/elasticsearch/config/elasticsearch.yml

#USER elasticsearch && EXPOSE 9200 9300