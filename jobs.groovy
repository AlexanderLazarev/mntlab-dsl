def git = "MNT-Lab/mntlab-dsl"
def repo = "pyurchuk"

def gitURL = "https://github.com/MNT-Lab/mntlab-dsl.git"
def command = "git ls-remote -h $gitURL"

def proc = command.execute()
proc.waitFor()

if ( proc.exitValue() != 0 ) {
    println "Error, ${proc.err.text}"
    System.exit(-1)
}

def branches = proc.in.text.readLines().collect {
    it.replaceAll(/[a-z0-9]*\trefs\/heads\//, '')
}

freeStyleJob('EPBYMINW6405/MNTLAB-pyurchuk-main-build-job'){
    description 'Building necessary jobs'
    
parameters {
     choiceParam('BRANCH_NAME', ['pyurchuk', 'master'], 'Select the branch')
        activeChoiceParam('BUILDS_STRIGGER') {
            description('Select child job')
            choiceType('CHECKBOX')
            groovyScript {
                script('["MNTLAB-pyurchuk-child1-build-job", "MNTLAB-pyurchuk-child2-build-job", "MNTLAB-pyurchuk-child3-build-job", "MNTLAB-pyurchuk-child4-build-job"]')
            }
        }
    }

scm {
        github(git, '$BRANCH_NAME')
}

steps {
    downstreamParameterized {
        trigger('$BUILDS_STRIGGER') {
            block {
                buildStepFailure('FAILURE')
                failure('FAILURE')
                unstable('UNSTABLE')
            }
            parameters {
                predefinedProp('BRANCH_NAME', '${BRANCH_NAME}')
            }
        }   

    }
}
}   

1.upto(4){
freeStyleJob("EPBYMINW6405/MNTLAB-pyurchuk-child${it}-build-job") {
    description "Creating children job"
    parameters {
    choiceParam('BRANCH_NAME', branches)
    }
    scm {
        github(git, '$BRANCH_NAME')
    }

/*steps {    
    copyArtifacts('EPBYMINW6405/MNTLAB-pyurchuk-main-build-job') {
        includePatterns('script.sh')
        targetDirectory('./')
        flatten()
        optional()
        buildSelector {
        latestSuccessful(true)
        }
    }
}

steps {
    shell('chmod +x script.sh && ./script.sh > output.txt && cat output.txt && tar -czf  ${BRANCH_NAME}_dsl_script.tar.gz output.txt jobs.groovy script.sh')
}

publishers {
        archiveArtifacts {
            pattern('output.txt')
            pattern('${BRANCH_NAME}_dsl_script.tar.gz')
            onlyIfSuccessful()
            }
        }*/
    }
}
