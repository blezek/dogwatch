



watch: config
	gulp watch


install: config
	env BUILD=production gulp build
	rsync -r public ../src/main/resources/

config: node_modules bower_components

node_modules: package.json
	npm install

bower_components: bower.json
	bower --config.interactive=false install
