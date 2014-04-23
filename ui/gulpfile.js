/*
install:
npm install --save gulp gulp-uglify gulp-concat gulp-notify gulp-cache gulp-livereload tiny-lr gulp-util express gulp-browserify streamqueue gulp-styl gulp-uglify gulp-rename
*/



var gulp = require('gulp'),
    concat = require('gulp-concat'),
    notify = require('gulp-notify'),
    refresh = require('gulp-livereload'),
    livereload = require('gulp-livereload'),
    uglify = require('gulp-uglify'),
    lr = require('tiny-lr'),
    streamqueue = require('streamqueue'),
    uglify = require('gulp-uglify'),
    rename = require('gulp-rename'),
    server = lr();

gulp.task("default", ['watch'], function() {
})

gulp.task("build", ['ace', 'assets', 'vendor', 'app', 'style', 'bootstrap'], function() {
})


gulp.task("watch", ['lr-server', 'build'], function() {
  console.log("\nStarting webserver and watching files\n")
  gulp.watch ( ['app/*.js', 'app/partials/**', 'app/*.html', 'app/assets/css/*.css'], ['style', 'app'])
})

// Handlebars / ember / all the rest
gulp.task('app', function() {

  // Just for backbone
  gulp.src('app/*.js')
  .pipe(gulp.dest('public/js'));

  gulp.src('app/*.html')
  .pipe(gulp.dest('public/'));

  gulp.src('app/images/*')
  .pipe(gulp.dest('public/images'));

  // Copy partials
  gulp.src('app/partials/**')
  .pipe(gulp.dest('public/partials'))
  .pipe(refresh(server))

})

// CSS using Styl
gulp.task('style', function() {
  gulp.src([
    'app/assets/*.css',
    'app/styles/*.css',
    'bower_components/font-awesome/css/font-awesome*.css'
    ])
//  .pipe(styl({compress : true }))
//  .pipe(stylus)
  .pipe(gulp.dest('public/css')).pipe(refresh(server));
})


// Vended source
gulp.task('vendor', function() {
  gulp.src([
    'bower_components/jquery/jquery.js',
    'bower_components/angular-ui-ace/ui-ace.js',
    'bower_components/angular-ui-bootstrap-bower/ui-bootstrap-tpls.js',
    'bower_components/vex/js/vex.dialog.js',
    'bower_components/vex/js/vex.js',
    'bower_components/angular-ui-router/release/angular-ui-router.js',
    'bower_components/angular-ui-select/dist/select.js',
    'bower_components/angularAMD/angularAMD.js',
    'bower_components/angularAMD/ngload.js',
    'bower_components/requirejs/require.js',
    'bower_components/backbone/backbone.js',
    'bower_components/angular/angular.js',
    'bower_components/angular-route/angular-route.js',
    'bower_components/underscore/underscore.js',
    'bower_components/handlebars/handlebars.js',
    'bower_components/ember/ember.js',
    'vendor/scripts/console-polyfill.js',
    'bower_components/jquery/dist/jquery.js',
    'bower_components/momentjs/moment.js',
    'bower_components/jstz-detect/jstz.js'
    ])
  .pipe(uglify({outSourceMap: true}))
  .pipe(gulp.dest('public/js'))

  gulp.src(['bower_components/dropzone/downloads/dropzone-amd-module.js'])
  .pipe(rename('dropzone.js'))
  .pipe(uglify({outSourceMap: true}))
  .pipe(gulp.dest('public/js'))

  gulp.src(['bower_components/font-awesome/fonts/fontawesome-webfont.ttf',
  'bower_components/font-awesome/fonts/fontawesome-webfont.woff'])
  .pipe(gulp.dest('public/fonts'))

})

gulp.task('ace', function() {
  gulp.src('bower_components/ace-builds/src-noconflict/**')
  .pipe(gulp.dest("public/js/ace"))
})

gulp.task('bootstrap', function() {
  gulp.src('bower_components/bootstrap/dist/**')
  .pipe(gulp.dest("public/"))
})

// Assets
gulp.task('assets', function() {
  gulp.src('app/assets/**')
  .pipe(gulp.dest("public/"))

})


gulp.task('lr-server', function() {
  server.listen(35729, function(err) {
    if (err) return console.log(err);
  });
});
