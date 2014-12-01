/*
 * Copyright (c) 2014 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var rUrl = /((([A-Za-z]{3,9}:(?:\/\/)?)(?:[-;:&=\+\$,\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\+\$,\w]+@)[A-Za-z0-9.-]+)((?:\/[\+~%\/.\w-_]*)?\??(?:[-\+=&;%@.\w_]*)#?(?:[.\!\/\\w]*))?)/;

/**
 * Fancybox tag
 *
 * Syntax:
 *   {% fancybox /path/to/image [/path/to/thumbnail] [title] %}
 */

hexo.extend.tag.register('fancybox', function (args) {
    var original = args.shift(),
            thumbnail = '';

    if (args.length && rUrl.test(args[0])) {
        thumbnail = args.shift();
    }

    var title = args.join(' ');

    return '<a class="fancybox" href="' + original + '" title="' + title + '">' +
           '<img src="' + (
            thumbnail || original
            ) + '" alt="' + title + '">'
    '</a>' +
    (
            title ? '<span class="caption">' + title + '</span>' : ''
    );
});
