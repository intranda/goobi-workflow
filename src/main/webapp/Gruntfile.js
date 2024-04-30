module.exports = function (grunt) {
	// ---------- VARIABLES ----------
	var banner = '';

	// ---------- PROJECT CONFIG ----------
	grunt.initConfig({
		theme: {
			name: 'goobiWorkflow'
		},
		pkg: grunt.file.readJSON('package.json'),
		src: {
			jsDevFolder: 'uii/template/js/dev/',
			jsDistFolder: 'uii/template/js/dist/',
			cssDistFolder: 'uii/template/css/dist/',
			lessDevFolder: 'uii/template/css/less/',
			uiiFolder: '/Users/marc.lettau-poelchen/g2g/goobi/application/goobi/uii/',
			resourcesFolder: '/Users/marc.lettau-poelchen/g2g/goobi/application/goobi/resources/',
		},
		less: {
			production: {
				options: {
					banner: banner,
					paths: ['<%=src.lessDevFolder%>'],
					plugins: [
						new (require('less-plugin-autoprefix'))({
							browsers: ["last 2 versions"],
							grid: true
						})
					],
					compress: true,
					sourceMap: true,
					outputSourceFiles: true,
				},
				files: {
					'<%=src.cssDistFolder%><%=theme.name%>.min.css': '<%=src.lessDevFolder%>build.less'
				}
			}
		},
		concat: {
			options: {
				banner: banner,
				sourceMap: true,
				separator: '\n',
			},
			distGoobiWorkflow: {
				src: [
					'<%=src.jsDevFolder%><%=theme.name%>JS.js',
					'<%=src.jsDevFolder%><%=theme.name%>JS.*.js'
				],
				dest: '<%=src.jsDistFolder%><%=theme.name%>JS.min.js'
			},
		},
		sync: {
			uii: {
				files: [{
					cwd: 'uii',
					src: ['**'],
					dest: '<%=src.uiiFolder%>',
				}],
				pretend: false,
				verbose: true,
				updateAndDelete: true,
			},
			resources: {
				files: [{
					cwd: 'resources',
					src: ['**'],
					dest: '<%=src.resourcesFolder%>',
				}],
				pretend: false,
				verbose: true,
				updateAndDelete: true,
			}
		},
		watch: {
			configFiles: {
				files: ['Gruntfile.js'],
				options: {
					reload: true
				}
			},
			css: {
				files: ['<%=src.lessDevFolder%>**/*.less'],
				tasks: ['less'],
				options: {
					spawn: false,
				}
			},
			static: {
				files: [
					'resources/**/*.xhtml',
					'uii/**/*.xhtml',
					'uii/**/*.html',
					'uii/**/*.jpg',
					'uii/**/*.png',
					'uii/**/*.svg',
					'uii/**/*.gif',
					'uii/**/*.ico',
					'uii/**/*.css',
					'uii/**/*.js',
				],
				tasks: [],
				options: {
					spawn: false,
				}
			},
			scripts: {
				files: [
					'<%=src.jsDevFolder%>*.js'
				],
				tasks: ['concat'],
				options: {
					spawn: false,
				}
			}
		}
	});

	// ---------- LOAD TASKS ----------
	grunt.loadNpmTasks('grunt-contrib-less');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-watch');
//	grunt.loadNpmTasks('grunt-sync');

	// ---------- REGISTER DEVELOPMENT TASKS ----------
	grunt.registerTask('default', ['watch']);
};