suite = {
  "mxversion" : "5.175.4",
  "name" : "xsite",
  "versionConflictResolution" : "latest",

  "javac.lint.overrides" : "none",

  "imports" : {
    "suites" : [
      {
        "name" : "common",
        "version" : "dd68c046a61b473532caf34784db409195de1d47",
        "urls" : [
          { "url" : "https://github.com/unknown-technologies/common", "kind" : "git" }
        ]
      },
    ],
  },

  "licenses" : {
    "GPLv3" : {
      "name" : "GNU General Public License, version 3",
      "url" : "https://opensource.org/licenses/GPL-3.0",
    }
  },

  "defaultLicense" : "GPLv3",

  "projects" : {
    "com.unknown.xsite" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "common:CORE",
        "common:SYNTAX"
      ],
      "javaCompliance" : "11+",
      "workingSets" : "xsite",
      "license" : "GPLv3",
    },

    "com.unknown.xsite.test" : {
      "subDir" : "projects",
      "sourceDirs" : ["src"],
      "dependencies" : [
        "com.unknown.xsite",
        "mx:JUNIT",
      ],
      "javaCompliance" : "11+",
      "workingSets" : "xsite",
      "license" : "GPLv3",
    }
  },

  "distributions" : {
    "XSITE" : {
      "path" : "build/xsite.jar",
      "subDir" : "xsite",
      "sourcesPath" : "build/xsite.src.zip",
      "dependencies" : [
        "com.unknown.xsite"
      ],
      "distDependencies" : [
        "common:CORE",
      ],
      "license" : "GPLv3",
    },

    "XSITE_STANDALONE" : {
      "path" : "build/xsite-standalone.jar",
      "sourcesPath" : "build/xsite-standalone.src.zip",
      "subDir" : "xsite",
      "mainClass" : "com.unknown.xsite.XSiteGenerator",
      "dependencies" : [
        "com.unknown.xsite"
      ],
      "overlaps" : [
        "common:CORE",
        "common:SYNTAX"
      ],
      "license" : "GPLv3",
    }
  }
}
