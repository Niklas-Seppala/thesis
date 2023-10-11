BINARY=wordindex
CODEDIRS=. src
JVM_HEADERS= /usr/lib/jvm/java-19-openjdk-amd64/include /usr/lib/jvm/java-19-openjdk-amd64/include/linux
INCDIRS=./include/ $(JVM_HEADERS)
CC=gcc
SANITIZER=
DEBUG=
DEPFLAGS=-MP -MD
OUT=out/
OPT=-O3
RT_NULL_CHECKS=-DNULL_CHECKS -DNULL_KILLS
CC_WARN=-Wall -Wshadow -Wextra -Wformat=2 -Wpedantic -fmax-errors=10 -Wno-unknown-pragmas
CFLAGS=${CC_WARN} $(OPT) -std=gnu11 ${DEBUG} -DVEC_DEF_CAP=8 ${RT_NULL_CHECKS} $(foreach D,$(INCDIRS),-I$(D)) ${DEPFLAGS}

CFILES=$(foreach D,$(CODEDIRS),$(wildcard $(D)/*.c))
OBJECTS=$(patsubst %.c,%.o,$(CFILES))
DEPFILES=$(patsubst %.c,%.d,$(CFILES))

-include $(DEPFILES)

.PHONY: all clean test_compile test

all: $(OUT)$(BINARY).so

$(OUT)$(BINARY).so: $(OBJECTS)
	$(CC) -shared -fPIC -o $@ $^

%.o:%.c
	$(CC) $(CFLAGS) -c -fPIC -o $@ $<

$(OUT)$(BINARY).test: $(OBJECTS) test/test.o
	$(CC) -o $@ $^ $(SANITIZER)

test_compile: $(OUT)$(BINARY).test
	

test: test_compile
	@./$(OUT)${BINARY}.test ${RUN_ARGS}

clean:
	@rm -rf $(OUT)/* $(OUT)$(BINARY).test $(OBJECTS) test/test.o $(DEPFILES) 2>/dev/null || true
