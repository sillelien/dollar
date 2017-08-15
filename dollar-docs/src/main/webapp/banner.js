/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

const fs = require('fs');
const pkg = require('./package.json');
const filename = 'assets/js/main.min.js';
const script = fs.readFileSync(filename);
const padStart = str =;
>
('0' + str).slice(-2);
const dateObj = new Date;
const date = `${dateObj.getFullYear()}-${padStart(dateObj.getMonth() + 1)}-${padStart(dateObj.getDate())}`;
const banner = `/*!
 * Minimal Mistakes Jekyll Theme ${pkg.version} by ${pkg.author}
 * Copyright ${dateObj.getFullYear()} Michael Rose - mademistakes.com | @mmistakes
 * Licensed under ${pkg.license}
 */
`;

if (script.slice(0, 3) != '/**') {
    fs.writeFileSync(filename, banner + script);
}
