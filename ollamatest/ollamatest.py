from ollama import Client

# --- configuration -------------------------------------------------
OLLAMA_HOST = "192.168.1.1"  # Docker host (the machine running the container)
OLLAMA_PORT = 11434            # Port you exposed with `-p 11434:11434`
MODEL      = "gpt-oss"         # Which model to query
# ------------------------------------------------------------------

# Create a client that talks to the remote Ollama server
client = Client(host=f"{OLLAMA_HOST}:{OLLAMA_PORT}")

# Send a prompt and print the full answer
response = client.generate(
    model=MODEL,
    prompt="Hello, world!",
    stream=False          # set to True for streaming output
)

print("Answer:", response["response"])
