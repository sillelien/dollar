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

var marked = require('marked'),
        renderer = new marked.Renderer();

var heading_anchor = function (data, options, callback) {
    var src = data.text.toString();
    renderer.heading = function (text, level) {
        var escapedText = text.toLowerCase().replace(/[\s]+/g, '-');

        return '<h' + level + ' id="' + escapedText + '">' + text + '</h' + level + '>';
    };

    return marked(src, {renderer: renderer});
};

hexo.extend.renderer.register('md', 'html', heading_anchor, true);
hexo.extend.renderer.register('markdown', 'html', heading_anchor, true);
hexo.extend.renderer.register('mkd', 'html', heading_anchor, true);
hexo.extend.renderer.register('mkdn', 'html', heading_anchor, true);
hexo.extend.renderer.register('mdwn', 'html', heading_anchor, true);
hexo.extend.renderer.register('mdtxt', 'html', heading_anchor, true);
hexo.extend.renderer.register('mdtext', 'html', heading_anchor, true);
