/**
 * WEBSITE: https://themefisher.com
 * TWITTER: https://twitter.com/themefisher
 * FACEBOOK: https://www.facebook.com/themefisher
 * GITHUB: https://github.com/themefisher/
 */

(function ($) {
	'use strict';

	// This function runs once the entire page is loaded
	$(document).ready(function () {
		// Sticky Menu
		$(window).scroll(function () {
			var height = $('.top-header').innerHeight();
			if ($('header').offset().top > 10) {
				$('.top-header').addClass('hide');
				$('.navigation').addClass('nav-bg');
				$('.navigation').css('margin-top', '-' + height + 'px');
			} else {
				$('.top-header').removeClass('hide');
				$('.navigation').removeClass('nav-bg');
				$('.navigation').css('margin-top', '-' + 0 + 'px');
			}
		});

		// navbarDropdown
		if ($(window).width() < 992) {
			$('.navigation .dropdown-toggle').on('click', function () {
				$(this).siblings('.dropdown-menu').animate({
					height: 'toggle'
				}, 300);
			});
		}

		// Background-images
		$('[data-background]').each(function () {
			$(this).css({
				'background-image': 'url(' + $(this).data('background') + ')'
			});
		});

		// Hero Slider
		$('.hero-slider').slick({
			autoplay: true,
			autoplaySpeed: 7500,
			pauseOnFocus: false,
			pauseOnHover: false,
			infinite: true,
			arrows: true,
			fade: true,
			prevArrow: '<button type=\'button\' class=\'prevArrow\'><i class=\'ti-angle-left\'></i></button>',
			nextArrow: '<button type=\'button\' class=\'nextArrow\'><i class=\'ti-angle-right\'></i></button>',
			dots: true
		});

		// This part enables the fade-in/out animations on the text
		$('.hero-slider').slickAnimation();

		// venobox popup
		$('.venobox').venobox();

		// filter
		var containerEl = document.querySelector('.filtr-container');
		if (containerEl) {
			var filterizd = $('.filtr-container').filterizr({});
		}
		// Active changer
		$('.filter-controls li').on('click', function () {
			$('.filter-controls li').removeClass('active');
			$(this).addClass('active');
		});
	});

	//  Count Up
	function counter() {
		var oTop;
		if ($('.count').length !== 0) {
			oTop = $('.count').offset().top - window.innerHeight;
		}
		if ($(window).scrollTop() > oTop) {
			$('.count').each(function () {
				var $this = $(this),
					countTo = $this.attr('data-count');
				$({
					countNum: $this.text()
				}).animate({
					countNum: countTo
				}, {
					duration: 1000,
					easing: 'swing',
					step: function () {
						$this.text(Math.floor(this.countNum));
					},
					complete: function () {
						$this.text(this.countNum);
					}
				});
			});
		}
	}
	$(window).on('scroll', function () {
		counter();
	});

})(jQuery);