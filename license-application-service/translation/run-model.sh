#!/bin/bash

echo "Ollama needs to be installed and a model downloaded before running this script."

echo "Downloading Mistral model from Ollama..."
ollama pull mistral

echo "Running Mistral model..."
ollama create mistral -f Modelfile
ollama run mistral

echo "done!"
