# Autotesting with Playwright and Locust

This directory contains files to set up and run autotests using Playwright and Locust.

## Prerequisites

- [Python 3.7](https://www.python.org/downloads/) version 3.7 or higher is required.

## Install

1. Run the `ìnit.sh` or `init.ps1` script to set up the Python virtual environment and install required dependencies.

2. Launch the system under test: Launch the backend Docker stack and make sure it is accessible under `localhost:8080` and authenticated.

   - For Playwright: Run the frontend application in development mode using the `npm dev` run config and make sure it is accessible under `localhost:3000`.
   - For Locust: Ensure the frontend application is not running, as Locust will simulate user interactions directly with the backend API.

## Run Playwright Tests

To run the Playwright tests, use the following commands:

Linux/macOS:
```bash
./run-e2e-tests.sh
```
Windows:
```powershell
.\run-e2e-tests.ps1
```

## Run Locust Tests

To run the Locust tests, use the following commands:

Linux/macOS:
```bash
./run-load-tests.sh
```
Windows:
```powershell
.\run-load-tests.ps1
```

> The script will run Locust with the test configuration defined in `locustfile.py`.
> Don't stop the script while testing, as it keeps the Locust server running.

Once Locust is running, open [http://localhost:8089](http://localhost:8089) in your browser to access the Locust web interface.

There, you can configure and start your performance tests:

- **Number of users to simulate**: Set the total number of clients to simulate (or keep default).
- **Ramp-up**: Set the number of clients started per second (or keep default).
- **Start the test by clicking the `START` button.**


## Test Structure

- Playwright test config is located in the `playwright` directory.
- Locust test config is located in the `locust` directory.
- Common test utilities are located in the `testkit` directory in the project root and are shared between Playwright and Locust tests. They contain the following:
  - Config and stateless data (e.g. URLs, state enums) in `testkit/config.py`
  - Data models containing runtime state in `testkit/models.py`
  - Data factory for generating test data in `testkit/data_factory.py`
  - Test scenarios in `testkit/scenarios.py`
  - API functions in `testkit/api_client.py` (for Playwright shortcuts and Locust)
  - UI functions in `testkit/ui_client.py` (for Playwright)
  - Utility functions in `testkit/utils.py`
