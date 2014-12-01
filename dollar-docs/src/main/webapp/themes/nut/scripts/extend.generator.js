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

var generator = hexo.extend.generator,
        config = hexo.config;

var tagIndex = function (locals, render, callback) {

    var tagDir = config.tag_dir + '/';

    var tags = locals.tags;

    render(tagDir, ['tag', 'archive', 'index'], {tags: tags, type: 'index'});

    callback();
};

var catIndex = function (locals, render, callback) {

    var catDir = config.category_dir + '/';

    var cats = locals.categories;

    render(catDir, ['category', 'archive', 'index'], {cats: cats, type: 'index'});

    callback();
};

var archiveIndex = function (locals, render, callback) {

    var archiveDir = config.archive_dir + '/';

    var posts = locals.posts;

    render(archiveDir, ['archive', 'index'], {posts: posts, type: 'index'});

    callback();
};

var wikiIndex = function (locals, render, callback) {
    var wikiDir = hexo._themeConfig.wiki_dir + '/',
            reg = new RegExp('^' + wikiDir),
            wikis = new Array();

    var pages = locals.pages;

    pages.each(function (page) {
        if (reg.test(page.path)) {
            wikis.push(page);
        }
    });

    render(wikiDir, ['wiki', 'index'], {wikis: wikis, title: 'Wiki'});

    callback();
};

generator.register(tagIndex);
generator.register(catIndex);
generator.register(archiveIndex);
generator.register(wikiIndex);
