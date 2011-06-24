#! /bin/bash

export T2O_HOME=/Volumes/Dev/google/text2onto

export GATE_HOME=/Volumes/Apps/GATE-4.0

export LIB=${T2O_HOME}/3rdparty

export T2O_CP=${T2O_HOME}/build/bin/:${LIB}/gate/gate.jar:${LIB}/gate/ontotext.jar:${LIB}/gate/jasper-compiler-jdt.jar:${LIB}/gate/heptag.jar:${LIB}/gate/xstream-1.0.2.jar:${LIB}/gate/xpp3-1.1.3.3_min.jar:${LIB}/gnu-regexp-1.0.8.jar:${LIB}/jdom.jar:${LIB}/jgraph/jgraph.jar:${LIB}/jgraph/jgraphaddons.jar:${LIB}/eclipse/workbench.jar:${LIB}/eclipse/runtime.jar:${LIB}/eclipse/jface.jar:${LIB}/eclipse/ws/win32/swt.jar:${LIB}/jwnl/jwnl.jar:${LIB}/commons-logging.jar:${LIB}/kaon/kaonapi.jar:${LIB}/kaon/apionrdf.jar:${LIB}/kaon/rdfapi.jar:${LIB}/kaon/query.jar:${LIB}/kaon/datalog.jar:${LIB}/kaon2/kaon2.jar:${LIB}/aclibico.jar:${LIB}/gate/xercesImpl.jar:${LIB}/gate/nekohtml-0.9.5.jar:${LIB}/PDFBox-0.7.2.jar:${LIB}/pellet/owlapi-bin.jar:${LIB}/pellet/pellet-core.jar:${LIB}/pellet/pellet-owlapi.jar:${LIB}/pellet/pellet-query.jar:${LIB}/pellet/pellet-datatypes.jar:${LIB}/pellet/pellet-owlapiv3.jar:${LIB}/pellet/pellet-rules.jar:${LIB}/pellet/pellet-el.jar:${LIB}/pellet/relaxngDatatype.jar:${LIB}/pellet/aterm-java-1.6.jar

java -ms2000M -mx2000M -Dgate.home=${GATE_HOME} -Dgate.config=${GATE_HOME}/gate.xml -Dgate.plugins.home=${GATE_HOME}/plugins -Dload.plugin.path=file:${GATE_HOME}/plugins/ANNIE -classpath ${T2O_CP} org.ontoware.text2onto.debug.TestAlgorithms file:/Volumes/Data/corpus/corpus_tourism/ file:/Volumes/Dev/temp/ontology
