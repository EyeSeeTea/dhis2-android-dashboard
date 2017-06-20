#!/usr/bin/env bash

# Upload CodeCov report
bash <(curl -s https://codecov.io/bash) -t ${CODECOV_TOKEN}

