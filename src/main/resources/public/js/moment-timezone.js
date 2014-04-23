(function(){function t(t){function n(t){t+="";var e=t.split(":"),n=~t.indexOf("-")?-1:1,s=Math.abs(+e[0]),r=parseInt(e[1],10)||0,i=parseInt(e[2],10)||0;return n*(60*s+r+i/60)}function s(t,e,s,r,i,u,a,o,h,l){this.name=t,this.startYear=+e,this.endYear=+s,this.month=+r,this.day=+i,this.dayRule=+u,this.time=n(a),this.timeRule=+o,this.offset=n(h),this.letters=l||"",this.date=f(this.date),this.weekdayAfter=f(this.weekdayAfter),this.lastWeekday=f(this.lastWeekday)}function r(t,e){this.rule=e,this.start=e.start(t)}function i(t,e){return t.isLast?-1:e.isLast?1:e.start-t.start}function u(t){this.name=t,this.rules=[],this.lastYearRule=f(this.lastYearRule)}function a(e,s,r,i,u,a){var o,h="string"==typeof u?u.split("_"):[9999];for(this.name=e,this.offset=n(s),this.ruleSet=r,this.letters=i,this.lastRule=f(this.lastRule),o=0;o<h.length;o++)h[o]=+h[o];this.until=t.utc(h).subtract("m",n(a))}function o(t,e){return t.until-e.until}function h(t){this.name=c(t),this.displayName=t,this.zones=[],this.zoneAndRule=f(this.zoneAndRule,function(t){return+t})}function f(t,e){var n={};return function(s){var r=e?e.apply(this,arguments):s;return r in n?n[r]:n[r]=t.apply(this,arguments)}}function l(t){var e,n,s;for(e in t)for(s=t[e],n=0;n<s.length;n++)d(e+"	"+s[n])}function d(t){if(A[t])return A[t];var e=t.split(/\s/),n=c(e[0]),r=new s(n,e[1],e[2],e[3],e[4],e[5],e[6],e[7],e[8],e[9],e[10]);return A[t]=r,y(n).add(r),r}function c(t){return(t||"").toLowerCase().replace(/\//g,"_")}function m(t){var e,n,s;for(e in t)for(s=t[e],n=0;n<s.length;n++)p(e+"	"+s[n])}function z(t){var e;for(e in t)L[c(e)]=c(t[e])}function p(t){if(k[t])return k[t];var e=t.split(/\s/),n=c(e[0]),s=new a(n,e[1],y(e[2]),e[3],e[4],e[5]);return k[t]=s,v(e[0]).add(s),s}function y(t){return t=c(t),b[t]||(b[t]=new u(t)),b[t]}function v(t){var e=c(t);return L[e]&&(e=L[e]),M[e]||(M[e]=new h(t)),M[e]}function R(t){t&&(t.zones&&m(t.zones),t.rules&&l(t.rules),t.links&&z(t.links))}function w(){var t,e=[];for(t in M)e.push(M[t]);return e}var g,Y=t.fn.zoneName,_=t.fn.zoneAbbr,A={},b={},k={},M={},L={},x=1,N=2,W=7,q=8;return void 0!==t.tz?t:(s.prototype={contains:function(t){return t>=this.startYear&&t<=this.endYear},start:function(e){return e=Math.min(Math.max(e,this.startYear),this.endYear),t.utc([e,this.month,this.date(e),0,this.time])},date:function(t){return this.dayRule===W?this.day:this.dayRule===q?this.lastWeekday(t):this.weekdayAfter(t)},weekdayAfter:function(e){for(var n=this.day,s=t([e,this.month,1]).day(),r=this.dayRule+1-s;n>r;)r+=7;return r},lastWeekday:function(e){var n=this.day,s=n%7,r=t([e,this.month+1,1]).day(),i=t([e,this.month,1]).daysInMonth(),u=i+(s-(r-1))-7*~~(n/7);return s>=r&&(u-=7),u}},r.prototype={equals:function(t){return t&&t.rule===this.rule?Math.abs(t.start-this.start)<864e5:!1}},u.prototype={add:function(t){this.rules.push(t)},ruleYears:function(t,e){var n,s,u,a=t.year(),o=[];for(n=0;n<this.rules.length;n++)s=this.rules[n],s.contains(a)?o.push(new r(a,s)):s.contains(a+1)&&o.push(new r(a+1,s));return o.push(new r(a-1,this.lastYearRule(a-1))),e&&(u=new r(a-1,e.lastRule()),u.start=e.until.clone().utc(),u.isLast=e.ruleSet!==this,o.push(u)),o.sort(i),o},rule:function(t,e,n){var s,r,i,u,a,o=this.ruleYears(t,n),h=0;for(n&&(r=n.offset+n.lastRule().offset,i=9e4*Math.abs(r)),a=o.length-1;a>-1;a--)u=s,s=o[a],s.equals(u)||(n&&!s.isLast&&Math.abs(s.start-n.until)<=i&&(h+=r-e),s.rule.timeRule===N&&(h=e),s.rule.timeRule!==x&&s.start.add("m",-h),h=s.rule.offset+e);for(a=0;a<o.length;a++)if(s=o[a],t>=s.start&&!s.isLast)return s.rule;return g},lastYearRule:function(t){var e,n,s,r=g,i=-1e30;for(e=0;e<this.rules.length;e++)n=this.rules[e],t>=n.startYear&&(s=n.start(t),s>i&&(i=s,r=n));return r}},a.prototype={rule:function(t,e){return this.ruleSet.rule(t,this.offset,e)},lastRule:function(){return this.rule(this.until)},format:function(t){return this.letters.replace("%s",t.letters)}},h.prototype={zoneAndRule:function(t){var e,n,s;for(t=t.clone().utc(),e=0;e<this.zones.length&&(n=this.zones[e],!(t<n.until));e++)s=n;return[n,n.rule(t,s)]},add:function(t){this.zones.push(t),this.zones.sort(o)},format:function(t){var e=this.zoneAndRule(t);return e[0].format(e[1])},offset:function(t){var e=this.zoneAndRule(t);return-(e[0].offset+e[1].offset)}},t.updateOffset=function(t,e){var n;t._z&&(n=t._z.offset(t),Math.abs(n)<16&&(n/=60),t.zone(n,e))},t.fn.tz=function(e){return e?(this._z=v(e),this._z&&t.updateOffset(this),this):this._z?this._z.displayName:void 0},t.fn.zoneName=function(){return this._z?this._z.format(this):Y.call(this)},t.fn.zoneAbbr=function(){return this._z?this._z.format(this):_.call(this)},t.momentProperties._z=null,t.tz=function(){var e,n=[],s=arguments.length-1;for(e=0;s>e;e++)n[e]=arguments[e];var r=t.apply(null,n),i=r.zone();return r.tz(arguments[s]),r.add("minutes",r.zone()-i)},t.tz.add=R,t.tz.addRule=d,t.tz.addZone=p,t.tz.zones=w,t.tz.version=e,t.tz.zoneExists=function(t){return v(t).zones.length>0},g=d("- 0 9999 0 0 0 0 0 0"),t)}var e="0.0.6";"function"==typeof define&&define.amd?define("moment-timezone",["moment"],t):"undefined"!=typeof module?module.exports=t(require("moment")):"undefined"!=typeof window&&window.moment&&t(window.moment)}).apply(this);