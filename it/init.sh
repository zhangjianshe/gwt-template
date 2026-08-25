mkdir -p "$PWD/data/step-ca-data/certs"
sudo chown -R 1000:1000 "$PWD/data/step-ca-data"
docker run --rm -it -v "$PWD/data/step-ca-data:/home/step" hub.cangling.cn/docker/smallstep/step-ca:latest step ca init

echo QAZwsx@1234 | sudo tee "$PWD/data/step-ca-data/secrets/password"
sudo chown -R 1000:1000 "$PWD/data/step-ca-data/secrets/password"

