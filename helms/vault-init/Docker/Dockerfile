FROM hashicorp/vault:1.14
RUN mkdir /app
WORKDIR /app  
COPY run.sh /app/
RUN ["chmod", "+x", "/app/run.sh"]
CMD [ "./app/run.sh" ]