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

/* ==========================================================================
   jQuery plugin settings and other scripts
   ========================================================================== */

$(document).ready(function () {

    // Sticky footer
    var bumpIt    = function () {
            $('body').css('margin-bottom', $('.page__footer').outerHeight(true));
        },
        didResize = false;

    bumpIt();

    $(window).resize(function () {
        didResize = true;
    });
    setInterval(function () {
        if (didResize) {
            didResize = false;
            bumpIt();
        }
    }, 250);

    // FitVids init
    $("#main").fitVids();

    // Follow menu drop down
    $(".author__urls-wrapper button").on("click", function () {
        $(".author__urls").toggleClass("is--visible");
        $(".author__urls-wrapper button").toggleClass("open");
    });

    // init smooth scroll
    $("a").smoothScroll({offset: -20});

    // add lightbox class to all image links
    $("a[href$='.jpg'],a[href$='.jpeg'],a[href$='.JPG'],a[href$='.png'],a[href$='.gif']").addClass("image-popup");

    // Magnific-Popup options
    $(".image-popup").magnificPopup({
                                        // disableOn: function() {
                                        //   if( $(window).width() < 500 ) {
                                        //     return false;
                                        //   }
                                        //   return true;
                                        // },
                                        type:                'image',
                                        tLoading:            'Loading image #%curr%...',
                                        gallery:             {
                                            enabled:            true,
                                            navigateByImgClick: true,
                                            preload:            [0, 1] // Will preload 0 - before current, and 1 after the current image
                                        },
                                        image:               {
                                            tError: '<a href="%url%">Image #%curr%</a> could not be loaded.',
                                        },
                                        removalDelay:        500, // Delay in milliseconds before popup is removed
                                        // Class that is added to body when popup is open.
                                        // make it unique to apply your CSS animations just to this exact popup
                                        mainClass:           'mfp-zoom-in',
                                        callbacks:           {
                                            beforeOpen: function () {
                                                // just a hack that adds mfp-anim class to markup
                                                this.st.image.markup = this.st.image.markup.replace('mfp-figure',
                                                                                                    'mfp-figure mfp-with-anim');
                                            }
                                        },
                                        closeOnContentClick: true,
                                        midClick:            true // allow opening popup on middle mouse click. Always set it to true if you don't provide alternative source.
                                    });

});
