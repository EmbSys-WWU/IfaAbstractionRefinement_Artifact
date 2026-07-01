CXX_FILES = \
	main.cpp 


H_FILES = 

bin_PROGRAMS = sc_export

check_PROGRAMS = sc_export
TESTS = $(check_PROGRAMS) 

INCLUDES = -I$(top_srcdir)/include

sc_export_SOURCES = $(CXX_FILES) $(H_FILES)

sc_export_LDADD = -L$(top_srcdir)/lib-$(TARGET_ARCH) -lsystemc -lm

sc_export_INSTALLS = $(sc_export_SOURCES) 
