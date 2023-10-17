BINARY=wordindex
OUT=out/
CODEDIRS=. src
JVM_HEADERS= $(JAVA)/include $(JAVA)/include/linux
INCDIRS=./include/ $(JVM_HEADERS)
DEPFLAGS=-MP -MD
CFILES=$(foreach D,$(CODEDIRS),$(wildcard $(D)/*.c))
OBJECTS=$(patsubst %.c,%.o,$(CFILES))
DEPFILES=$(patsubst %.c,%.d,$(CFILES))
-include $(DEPFILES)

############## DEBUG BUILDS ###############
# UBSAN=-fsanitize=undefined
# ASAN=-fsanitize=address -static-libasan
# DEBUG=-g
# OPT=-O0
# RT_NULL_CHECKS=-DNULL_CHECKS -DNULL_KILLS
###########################################

############# RELEASE BUILDS ##############
OPT=-O3
###########################################

CC=gcc
CC_WARN=-Wall -Wshadow -Wextra -Wformat=2 -Wpedantic -fmax-errors=10 -Wno-unknown-pragmas
CFLAGS=${CC_WARN} $(OPT) -std=gnu11 ${DEBUG} -DVEC_DEF_CAP=8 ${RT_NULL_CHECKS} $(foreach D,$(INCDIRS),-I$(D)) ${DEPFLAGS}

.PHONY: all clean test_compile test mkdirs

all: mkdirs $(OUT)$(BINARY).so

$(OUT)$(BINARY).so: $(OBJECTS)
	$(CC) -shared -fPIC -o $@ $^

%.o:%.c
	$(CC) $(CFLAGS) -c -fPIC -o $@ $< $(ASAN) $(UBSAN)

$(OUT)$(BINARY).test: $(OBJECTS) test/test.o
	$(CC) -o $@ $^ $(ASAN) $(UBSAN)

test_compile: $(OUT)$(BINARY).test
	

test: test_compile
	@./$(OUT)${BINARY}.test ${RUN_ARGS}

clean:
	@rm -rf $(OUT)/* $(OUT)$(BINARY).test $(OBJECTS) test/test.o $(DEPFILES) 2>/dev/null || true

mkdirs:
	@mkdir -p $(OUT)