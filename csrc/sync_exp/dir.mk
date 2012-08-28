# sync_exp/dir.mk
# 
# Part of the CCNx distribution.
#
# Copyright (C) 2009-2012 Palo Alto Research Center, Inc.
#
# This work is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License version 2 as published by the
# Free Software Foundation.
# This work is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.
#

CCNLIBDIR = ../lib
LDLIBS = -L$(CCNLIBDIR) $(MORE_LDLIBS) -lccn

INSTALLED_PROGRAMS = 

PROGRAMS = $(INSTALLED_PROGRAMS) \
    SyncTest  \
    ccnsyncwatch

BROKEN_PROGRAMS =
DEBRIS =
SCRIPTSRC = 
CSRC = \
 IndexSorter.c \
 SyncActions.c \
 SyncBase.c \
 SyncHashCache.c \
 SyncNode.c \
 SyncRoot.c \
 SyncTest.c \
 SyncTreeWorker.c \
 SyncUtil.c \
 UpdateSketch.c \
 sync_api.c \
 sync_diff.c \
 ccnsyncwatch.c 

HSRC = IndexSorter.h SyncActions.h SyncBase.h SyncHashCache.h SyncMacros.h SyncNode.h SyncPrivate.h SyncRoot.h SyncTreeWorker.h SyncUtil.h sync.h sync_depends.h sync_diff.h

default all: $(PROGRAMS)
all: $(BROKEN_PROGRAMS)

test: default

$(PROGRAMS): $(CCNLIBDIR)/libccn.a

OBJ_GROUP1 = IndexSorter.o SyncBase.o SyncHashCache.o SyncNode.o SyncRoot.o SyncTreeWorker.o SyncUtil.o
OBJ_GROUP2 = $(OBJ_GROUP1) sync_diff.o sync_api.o

# IndexSorter.o SyncActions.o SyncBase.o SyncHashCache.o SyncNode.o SyncRoot.o SyncTreeWorker.o SyncUtil.o SyncTest.o

SyncTest_OBJ = SyncTest.o $(OBJ_GROUP1)
SyncTest: $(SyncTest_OBJ)
	$(CC) $(CFLAGS) -o $@ $(SyncTest_OBJ) $(LDLIBS) $(OPENSSL_LIBS) -lcrypto

ccnsyncwatch_OBJ = ccnsyncwatch.o $(OBJ_GROUP2)
ccnsyncwatch: $(ccnsyncwatch_OBJ)
	$(CC) $(CFLAGS) -o $@ $(ccnsyncwatch_OBJ) $(LDLIBS) $(OPENSSL_LIBS) -lcrypto

clean:
	rm -f *.o *.a $(PROGRAMS) $(BROKEN_PROGRAMS) depend
	rm -rf *.dSYM *.gcov *.gcda *.gcno $(DEBRIS)

check test: SyncTest $(SCRIPTSRC)
	@echo No sync_exp tests hooked up yet.
	: -------------------------- :
	:  sync_exp unit tests pass  :
	: -------------------------- :

###############################
# Dependencies below here are checked by depend target
# but must be updated manually.
###############################
IndexSorter.o: IndexSorter.c IndexSorter.h
SyncActions.o: SyncActions.c ../include/ccn/ccn.h ../include/ccn/coding.h \
  ../include/ccn/charbuf.h ../include/ccn/indexbuf.h \
  ../include/ccn/schedule.h SyncActions.h SyncBase.h \
  ../include/ccn/loglevels.h sync_depends.h SyncRoot.h SyncUtil.h \
  IndexSorter.h SyncNode.h SyncMacros.h SyncPrivate.h SyncTreeWorker.h \
  SyncHashCache.h
SyncBase.o: SyncBase.c SyncMacros.h SyncBase.h ../include/ccn/loglevels.h \
  sync_depends.h ../include/ccn/ccn.h ../include/ccn/coding.h \
  ../include/ccn/charbuf.h ../include/ccn/indexbuf.h SyncPrivate.h \
  SyncRoot.h SyncUtil.h IndexSorter.h ../include/ccn/uri.h
SyncHashCache.o: SyncHashCache.c SyncBase.h ../include/ccn/loglevels.h \
  sync_depends.h ../include/ccn/ccn.h ../include/ccn/coding.h \
  ../include/ccn/charbuf.h ../include/ccn/indexbuf.h SyncHashCache.h \
  SyncNode.h SyncMacros.h SyncRoot.h SyncUtil.h IndexSorter.h
SyncNode.o: SyncNode.c SyncNode.h ../include/ccn/ccn.h \
  ../include/ccn/coding.h ../include/ccn/charbuf.h \
  ../include/ccn/indexbuf.h SyncMacros.h SyncRoot.h SyncUtil.h \
  IndexSorter.h
SyncRoot.o: SyncRoot.c ../include/ccn/ccn.h ../include/ccn/coding.h \
  ../include/ccn/charbuf.h ../include/ccn/indexbuf.h \
  ../include/ccn/digest.h ../include/ccn/schedule.h ../include/ccn/uri.h \
  ../include/ccn/loglevels.h SyncMacros.h SyncPrivate.h SyncBase.h \
  sync_depends.h SyncRoot.h SyncUtil.h IndexSorter.h SyncHashCache.h
SyncTest.o: SyncTest.c SyncActions.h ../include/ccn/charbuf.h SyncBase.h \
  ../include/ccn/loglevels.h sync_depends.h ../include/ccn/ccn.h \
  ../include/ccn/coding.h ../include/ccn/indexbuf.h SyncRoot.h SyncUtil.h \
  IndexSorter.h SyncHashCache.h SyncNode.h SyncMacros.h SyncPrivate.h \
  SyncTreeWorker.h ../include/ccn/digest.h ../include/ccn/fetch.h \
  ../include/ccn/uri.h ../include/ccn/seqwriter.h
SyncTreeWorker.o: SyncTreeWorker.c SyncMacros.h SyncNode.h \
  ../include/ccn/ccn.h ../include/ccn/coding.h ../include/ccn/charbuf.h \
  ../include/ccn/indexbuf.h SyncTreeWorker.h SyncHashCache.h SyncUtil.h \
  IndexSorter.h
SyncUtil.o: SyncUtil.c SyncBase.h ../include/ccn/loglevels.h \
  sync_depends.h ../include/ccn/ccn.h ../include/ccn/coding.h \
  ../include/ccn/charbuf.h ../include/ccn/indexbuf.h SyncHashCache.h \
  SyncNode.h SyncMacros.h SyncPrivate.h SyncRoot.h SyncUtil.h \
  IndexSorter.h ../include/ccn/uri.h
UpdateSketch.o: UpdateSketch.c
sync_api.o: sync_api.c ../include/ccn/ccn.h ../include/ccn/coding.h \
  ../include/ccn/charbuf.h ../include/ccn/indexbuf.h \
  ../include/ccn/digest.h ../include/ccn/loglevels.h \
  ../include/ccn/schedule.h ../include/ccn/uri.h \
  ../include/ccn/ccn_private.h sync.h sync_diff.h SyncHashCache.h \
  SyncRoot.h SyncTreeWorker.h SyncUtil.h IndexSorter.h SyncNode.h \
  SyncMacros.h SyncPrivate.h SyncBase.h sync_depends.h
sync_diff.o: sync_diff.c ../include/ccn/ccn.h ../include/ccn/coding.h \
  ../include/ccn/charbuf.h ../include/ccn/indexbuf.h \
  ../include/ccn/digest.h ../include/ccn/schedule.h ../include/ccn/sync.h \
  ../include/ccn/uri.h IndexSorter.h SyncNode.h SyncMacros.h \
  SyncPrivate.h SyncBase.h ../include/ccn/loglevels.h sync_depends.h \
  SyncRoot.h SyncUtil.h SyncTreeWorker.h SyncHashCache.h sync_diff.h
ccnsyncwatch.o: ccnsyncwatch.c ../include/ccn/ccn.h \
  ../include/ccn/coding.h ../include/ccn/charbuf.h \
  ../include/ccn/indexbuf.h ../include/ccn/uri.h
