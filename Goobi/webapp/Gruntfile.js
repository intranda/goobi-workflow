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
			jsDevFolder : 'js/dev/',
			jsDistFolder : 'js/dist/',
			jsAssetsFolder : 'assets/js/',
			cssAssetsFolder : 'assets/css/',
            cssDistFolder : 'css/dist/',
			lessDevFolder : 'css/less/',
			userFolder: '/Users/marc.lettau-poelchen/g2g/goobi/application/goobi/'
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
		copy: {
            main: {
                files: [
                    {
                        expand: true, 
                        src: [ 'uii/**' ], 
                        dest: '<%=src.userFolder%>',
                        rename: function(dest, src) {
                            return dest + ( src.replace(/^..\/src\/main\/resources\/frontend\/?/ ,"") );
                        }
                    } 
                ],
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
				tasks : [ 'less' ],
				options : {
					spawn : false,
				}
			},
			scripts : {
				files : [
					'<%=src.jsDevFolder%>*.js'
				],
				tasks : [ 'concat', 'uglify', 'copy' ],
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
	grunt.loadNpmTasks('grunt-contrib-copy');

	// ---------- REGISTER DEVELOPMENT TASKS ----------
	grunt.registerTask('default', [ 'copy', 'watch' ]);
};