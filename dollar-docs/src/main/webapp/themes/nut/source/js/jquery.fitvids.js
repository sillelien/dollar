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

/*global jQuery */
/*jshint multistr:true browser:true */


(
        function ($) {

            "use strict";

            $.fn.fitVids = function (options) {
                var settings = {
                    customSelector: null
                };

                if (!document.getElementById('fit-vids-style')) {

                    var div = document.createElement('div'),
                            ref = document.getElementsByTagName('base')[0] || document.getElementsByTagName('script')[0],
                            cssStyles = '&shy;<style>.fluid-width-video-wrapper{width:100%;position:relative;padding:0;}.fluid-width-video-wrapper iframe,.fluid-width-video-wrapper object,.fluid-width-video-wrapper embed {position:absolute;top:0;left:0;width:100%;height:100%;}</style>';

                    div.className = 'fit-vids-style';
                    div.id = 'fit-vids-style';
                    div.style.display = 'none';
                    div.innerHTML = cssStyles;

                    ref.parentNode.insertBefore(div, ref);

                }

                if (options) {
                    $.extend(settings, options);
                }

                return this.each(function () {
                    var selectors = [
                        "iframe[src*='player.youku.com']",
                        "iframe[src*='player.vimeo.com']",
                        "iframe[src*='youtube.com']",
                        "iframe[src*='youtube-nocookie.com']",
                        "iframe[src*='kickstarter.com'][src*='video.html']",
                        "object",
                        "embed"
                    ];

                    if (settings.customSelector) {
                        selectors.push(settings.customSelector);
                    }

                    var $allVideos = $(this).find(selectors.join(','));
                    $allVideos = $allVideos.not("object object"); // SwfObj conflict patch

                    $allVideos.each(function () {
                        var $this = $(this);
                        if (this.tagName.toLowerCase() === 'embed' && $this.attr('src').match('www.xiami.com')) { return; }
                        if (this.tagName.toLowerCase() === 'embed' && $this.parent('object').length || $this.parent('.fluid-width-video-wrapper').length) { return; }
                        var height = (
                                     this.tagName.toLowerCase() === 'object' || (
                                     $this.attr('height') && !isNaN(parseInt($this.attr('height'), 10))
                                     )
                                     ) ? parseInt($this.attr('height'), 10) : $this.height(),
                                width = !isNaN(parseInt($this.attr('width'), 10)) ? parseInt($this.attr('width'),
                                                                                             10) : $this.width(),
                                aspectRatio = height / width;
                        if (!$this.attr('id')) {
                            var videoID = 'fitvid' + Math.floor(Math.random() * 999999);
                            $this.attr('id', videoID);
                        }
                        $this.wrap('<div class="fluid-width-video-wrapper"></div>').parent('.fluid-width-video-wrapper').css('padding-top',
                                                                                                                             (
                                                                                                                             aspectRatio * 100
                                                                                                                             ) + "%");
                        $this.removeAttr('height').removeAttr('width');
                    });
                });
            };
// Works with either jQuery or Zepto
        }
)(window.jQuery || window.Zepto);
