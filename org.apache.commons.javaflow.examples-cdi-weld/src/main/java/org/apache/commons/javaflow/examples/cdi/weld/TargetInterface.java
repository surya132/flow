package org.apache.commons.javaflow.examples.cdi.weld;

import org.apache.commons.javaflow.api.continuable;

interface TargetInterface {

    @continuable void execute(String prefix);

}
