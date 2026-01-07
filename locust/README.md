# Autotesting with Locust

This directory contains files to set up and run performance tests using Locust.

## Prerequisites

- [Python 3.7](https://www.python.org/downloads/) version 3.7 or higher is required.

## Install and Run Locust

1. Add your client secret to the `.env` file (in `license-application-service`):

2. Launch the Run Config `license-application-service: Compose Deployment Scaled` to start the Docker services for frontend and backend.

3. Run the Bash/PowerShell script to install required dependencies and start Locust:

Linux/macOS:
```bash
./run-locust.sh
```
Windows:
```powershell
.\run-locust.ps1
```
> The script will run Locust with the test configuration defined in `locustfile.py`.
> Don't stop the script while testing, as it keeps the Locust server running.

## Access Locust Web Interface

Once Locust is running, open [http://localhost:8089](http://localhost:8089) in your browser to access the Locust web interface.

There, you can configure and start your performance tests:

- **Number of users to simulate**: Set the total number of clients to simulate (or keep default).
- **Ramp-up**: Set the number of clients started per second (or keep default).
- **Start the test by clicking the `START` button.**