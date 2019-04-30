module.exports = function(grunt) {
	// ---------- VARIABLES ----------
	var banner = '';

	// ---------- PROJECT CONFIG ----------
	grunt.initConfig({
        theme : {
			name : 'goobiWorkflow'
		},
		pkg : grunt.file.readJSON('package.json'),
		src : {
			jsDevFolder : 'uii/template/js/dev/',
			jsDistFolder : 'uii/template/js/dist/',
            cssDistFolder : 'uii/template/css/dist/',
			lessDevFolder : 'uii/template/css/less/',
			userFolder: '/Users/marc.lettau-poelchen/g2g/goobi/application/goobi/uii/'
		},
		less : {
			production : {
				options : {
					banner : banner,
					paths : [ '<%=src.lessDevFolder%>' ],
					plugins : [
						new ( require('less-plugin-autoprefix') )({
							browsers : [ "last 2 versions" ],
							grid : true
						})
					],
					compress : true,
					sourceMap : true,
					outputSourceFiles: true,
				},
				files : {
					'<%=src.cssDistFolder%><%=theme.name%>.min.css' : '<%=src.lessDevFolder%>build.less'
				}
			}
		},
		concat : {
			options : {
				banner : banner,
				separator : '\n',
				stripBanners : true,
				sourceMap : true
			},
			distGoobiWorkflow : {
				src : [
					'<%=src.jsDevFolder%><%=theme.name%>JS.js',
					'<%=src.jsDevFolder%><%=theme.name%>JS.*.js'
				],
				dest : '<%=src.jsDistFolder%><%=theme.name%>JS.min.js'
			},
		},
		uglify: {
			options: {
				banner: banner,
				compress: {
					drop_console: true
				}
			},
			GoobiWorkflow: {
				options: {
					mangle: {
						reserved: ['jQuery']
					},
					sourceMap: true,
					sourceMapName: '<%=src.jsDistFolder%><%=theme.name%>JS.map'
				},
				files: {
					'<%=src.jsDistFolder%><%=theme.name%>JS.min.js': ['<%=src.jsDistFolder%><%=theme.name%>JS.min.js']
				}
			}
		},
		sync: {
			main: {
				files: [{
					cwd: 'uii',
					src: [ '**' ],
					dest: '<%=src.userFolder%>',
				}],
				pretend: false,
				verbose: true,
				updateAndDelete: true,
			}
		},
		watch : {
			configFiles : {
				files : [ 'Gruntfile.js' ],
				options : {
					reload : true
				}
			},
			css : {
				files : [ '<%=src.lessDevFolder%>**/*.less' ],
				tasks : [ 'less', 'sync' ],
				options : {
					spawn : false,
				}
			},
			static : {
				files : [ 
					'uii/**/*.xhtml', 
					'uii/**/*.html',
					'uii/**/*.jpg',
					'uii/**/*.png',
					'uii/**/*.svg',
					'uii/**/*.gif',
					'uii/**/*.ico',
				],
				tasks : [ 'sync' ],
				options : {
					spawn : false,
				}
			},
			scripts : {
				files : [
					'<%=src.jsDevFolder%>*.js'
				],
				tasks : [ 'concat', 'uglify', 'sync' ],
				options : {
					spawn : false,
				}
			}
		}
	});
	
	// ---------- LOAD TASKS ----------
	grunt.loadNpmTasks('grunt-contrib-less');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-sync');

	// ---------- REGISTER DEVELOPMENT TASKS ----------
	grunt.registerTask('default', [ 'sync', 'watch' ]);
};