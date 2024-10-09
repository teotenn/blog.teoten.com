var shadow$provide = {};
(function(){
'use strict';/*

 Copyright The Closure Library Authors.
 SPDX-License-Identifier: Apache-2.0
*/
var a=this||self;function c(){var b=a.navigator;return b&&(b=b.userAgent)?b:""}function d(b){return-1!=c().indexOf(b)};function e(){return d("iPhone")&&!d("iPod")&&!d("iPad")};d("Opera");d("Trident")||d("MSIE");d("Edge");!d("Gecko")||-1!=c().toLowerCase().indexOf("webkit")&&!d("Edge")||d("Trident")||d("MSIE")||d("Edge");-1!=c().toLowerCase().indexOf("webkit")&&!d("Edge")&&d("Mobile");d("Macintosh");d("Windows");d("Linux")||d("CrOS");var f=a.navigator||null;f&&(f.appVersion||"").indexOf("X11");d("Android");e();d("iPad");d("iPod");e()||d("iPad")||d("iPod");c().toLowerCase().indexOf("kaios");document.getElementById("testing-js").addEventListener("click",function(){return alert("JS Works!")});
}).call(this);