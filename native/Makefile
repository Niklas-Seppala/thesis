BINARY=wordindex
CODEDIRS=. src
INCDIRS=./include/ /usr/lib/jvm/java-19-openjdk-amd64/include /usr/lib/jvm/java-19-openjdk-amd64/include/linux
CC=gcc
SANITIZER=
DEBUG=-g
MKDIR_P = mkdir -p
DEPFLAGS=-MP -MD
OUT=out/
OPT=-O0
RT_NULL_CHECKS=-DNULL_CHECKS -DNULL_KILLS
CC_WARN=-Wall -Wshadow -Wextra -Wformat=2 -Wpedantic -fmax-errors=10 -Wno-unknown-pragmas
CFLAGS=${CC_WARN} $(OPT) -std=gnu11 ${DEBUG} -DVEC_DEF_CAP=8 ${RT_NULL_CHECKS} $(foreach D,$(INCDIRS),-I$(D)) ${DEPFLAGS} -fno-stack-protector

CFILES=$(foreach D,$(CODEDIRS),$(wildcard $(D)/*.c))
OBJECTS=$(patsubst %.c,%.o,$(CFILES))
DEPFILES=$(patsubst %.c,%.d,$(CFILES))

all: $(OUT)$(BINARY).so

#$(BINARY).a: $(OBJECTS)
#	ar -r -o $@ $^

$(OUT)$(BINARY).so: $(OBJECTS)
	$(CC) -shared -fPIC -o $@ $^


%.o:%.c
	$(CC) $(CFLAGS) -c -fPIC -o $@ $<

clean:
	@rm -rf $(OUT)/* $(OUT)$(BINARY).test $(OBJECTS) test/test.o $(DEPFILES) 2>/dev/null || true

# include the dependencies
-include $(DEPFILES)

# add .PHONY so that the non-targetfile - rules work even if a file with the same name exists.
.PHONY: all clean distribute diff

$(OUT)$(BINARY).test: $(OBJECTS) test/test.o
	$(CC) -o $@ $^ $(SANITIZER)

test_compile: $(OUT)$(BINARY).test
	

test: test_compile
	@./$(OUT)${BINARY}.test ${RUN_ARGS}
