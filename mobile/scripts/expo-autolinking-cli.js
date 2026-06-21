#!/usr/bin/env node
'use strict';

const path = require('path');

process.chdir(path.join(__dirname, '..'));
require('expo-modules-autolinking/build')(process.argv.slice(2));
