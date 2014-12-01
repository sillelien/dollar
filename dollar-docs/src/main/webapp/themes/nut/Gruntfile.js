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

module.exports = function (grunt) {
    grunt.initConfig({
                         gitclone: {
                             fontawesome: {
                                 options: {
                                     repository: 'https://github.com/FortAwesome/Font-Awesome.git',
                                     directory: 'tmp/fontawesome'
                                 },
                             },
                             fancybox: {
                                 options: {
                                     repository: 'https://github.com/fancyapps/fancyBox.git',
                                     directory: 'tmp/fancybox'
                                 }
                             }
                         },
                         copy: {
                             fontawesome: {
                                 expand: true,
                                 cwd: 'tmp/fontawesome/fonts/',
                                 src: ['**'],
                                 dest: 'source/css/fonts/'
                             },
                             fancybox: {
                                 expand: true,
                                 cwd: 'tmp/fancybox/source/',
                                 src: ['**'],
                                 dest: 'source/fancybox/'
                             }
                         },
                         _clean: {
                             tmp: ['tmp'],
                             fontawesome: ['source/css/fonts'],
                             fancybox: ['source/fancybox']
                         }
                     });

    require('load-grunt-tasks')(grunt);

    grunt.renameTask('clean', '_clean');

    grunt.registerTask('fontawesome', ['gitclone:fontawesome', 'copy:fontawesome', '_clean:tmp']);
    grunt.registerTask('fancybox', ['gitclone:fancybox', 'copy:fancybox', '_clean:tmp']);
    grunt.registerTask('default', ['gitclone', 'copy', '_clean:tmp']);
    grunt.registerTask('clean', ['_clean']);
};
