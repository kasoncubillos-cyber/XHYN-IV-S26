// Optional private-input regression. No game script is bundled or written.
// Build: g++ -std=c++20 -O2 -Iglue/rexglue-sdk-main/gta4-recomp/src
//        -Iglue/rexglue-sdk-main/thirdparty tools/test_s26_phone_program.cpp -o /tmp/phone-real
// Run: /tmp/phone-real /path/to/decoded-spcellphonemain-code.bin
#include <filesystem>
#include <fstream>
#include <iostream>
#include <map>
#include <stdexcept>
#include <variant>
#include "gta4_phone_cheat_patch.h"
#include <picosha2/picosha2.h>

namespace {
using namespace gta4::quicksave;
using namespace gta4::quicksave::bytecode;
using namespace gta4::phone_cheats;
constexpr uint32_t kObserve=0x90000010, kRequest=0x90000020, kActivate=0x90000030;
size_t checks=0;
void Check(bool value) { ++checks; if(!value) throw std::runtime_error("Real phone program assertion failed"); }
using Value=std::variant<int64_t,std::string>;
struct Vm {
  std::vector<Value> stack;
  std::vector<std::pair<uint32_t,std::string>> options;
  unsigned requests=0, observations=0, closes=0;
  bool admit=false;
  int64_t Pop() { Check(!stack.empty()); auto v=std::get<int64_t>(stack.back()); stack.pop_back(); return v; }
  void Run(const std::vector<uint8_t>& code, uint32_t pc, uint32_t stop, const PhoneProfile& p) {
    for(unsigned steps=0;pc!=stop;++steps) {
      Check(steps<5000 && pc<code.size()); const auto op=code[pc];
      if(op>=80) { stack.push_back(int64_t(op)-96); ++pc; }
      else if(op==kPushS) { Check(pc+3<=code.size()); stack.push_back(int64_t(int16_t(uint16_t(code[pc+1])|(uint16_t(code[pc+2])<<8)))); pc+=3; }
      else if(op==kPush) { stack.push_back(int64_t(int32_t(Read32(code,pc+1)))); pc+=5; }
      else if(op==kLocal) { stack.push_back(int64_t(0x10000000)+Pop()*4); ++pc; }
      else if(op==kString) {
        Check(pc+2<=code.size()); auto n=code[pc+1];Check(n>0 && pc+2+n<=code.size() && code[pc+1+n]==0);
        stack.push_back(std::string(reinterpret_cast<const char*>(code.data()+pc+2),n-1));pc+=2+n;
      } else if(op==kJump) { pc=Read32(code,pc+1); }
      else if(op==kJumpFalse) { const auto target=Read32(code,pc+1);pc=Pop()?pc+5:target; }
      else if(op==kCall) {
        const auto target=Read32(code,pc+1);pc+=5;
        if(target==p.append_function) {
          Check(stack.size()>=p.append_args);const auto first=stack.size()-p.append_args;
          Check(std::get<int64_t>(stack[first])==0x10000000+int64_t(p.menu_local)*4);
          Check(std::get<int64_t>(stack[first+1])==0x10000000+int64_t(p.options_local)*4);
          options.emplace_back(uint32_t(std::get<int64_t>(stack[first+2])),std::get<std::string>(stack[first+3]));
          stack.resize(first);
        } else if(target==p.set_state_function) { Check(Pop()==kClosingPhoneState);++closes; }
        else throw std::runtime_error("Unexpected original helper invocation");
      } else if(op==kNative) {
        Check(pc+7<=code.size() && code[pc+1]==1);const auto returns=code[pc+2];const auto target=Read32(code,pc+3);pc+=7;
        Check(Pop()==0);
        if(target==kObserve) {Check(returns==0);++observations;}
        else if(target==kRequest) {Check(returns==1);++requests;stack.push_back(int64_t(admit));}
        else throw std::runtime_error("Listing/accept invoked an unexpected native");
      } else throw std::runtime_error("Unexpected instruction in generated phone path");
    }
  }
};
void Verify(const std::filesystem::path& input) {
  Check(std::filesystem::file_size(input)==kPhoneProfiles[0].code_size);
  std::ifstream file(input,std::ios::binary);std::vector<uint8_t> raw((std::istreambuf_iterator<char>(file)),{});
  std::string digest;picosha2::hash256_hex_string(raw.begin(),raw.end(),digest);
  const auto* profile=MatchPhoneProfile(uint32_t(raw.size()),digest);Check(profile && profile->episode==0);
  const auto& p=*profile;
  // Independent positions/values obtained from this exact admitted Xbox SCO.
  constexpr uint32_t root=0x2518, menu=0x500F, render=0x5066, accept=0xA6A9, resume=0xA6B6;
  Check(raw[root+6]==kPushS && raw[root+7]==8 && raw[root+8]==4); // state 1032
  auto linked=raw;const auto starts=detail::InstructionStarts(raw);
  std::map<uint32_t,uint32_t> thunks;unsigned activate_count=0;
  for(size_t at=0;at<raw.size();++at) if(starts[at] && raw[at]==kNative) {
    const auto hash=Read32(raw,at+3);
    uint32_t address;
    if(hash==0x5AA0D18E) {address=kActivate;++activate_count;}
    else {auto [it,inserted]=thunks.emplace(hash,0x90010000+uint32_t(thunks.size())*16);(void)inserted;address=it->second;}
    Write32(linked,at+3,address);
  }
  Check(activate_count==17);
  for(bool quicksave:{false,true}) {
    const auto patched=BuildCheatsPatch(linked,p,BuildPhonePatch(linked,p,0x90000000,kObserve,quicksave),kRequest,kActivate);
    Check(patched.code.size()>linked.size());
    for(size_t at=0;at<linked.size();++at) {
      bool changed=false;
      for(auto site:{p.append_site,p.accept_site,root,menu,accept}) changed|=at>=site && at<site+5;
      if(!changed) Check(patched.code[at]==linked[at]);
    }
    Vm root_vm;root_vm.Run(patched.code,p.append_site,p.append_site+5,p);
    Check(root_vm.options.size()==(quicksave?2u:1u));
    Check(root_vm.options[0]==std::pair<uint32_t,std::string>(1032,"LR_CHEAT"));
    if(quicksave) Check(root_vm.options[1]==std::pair<uint32_t,std::string>(kQuicksaveAction,std::string(kQuicksaveLabel)));
    Check(root_vm.requests==0 && root_vm.observations==1 && root_vm.stack.size()==2);
    Check(root_vm.Pop()==p.options_local);Check(root_vm.Pop()==0x10000000+int64_t(p.menu_local)*4);
    Vm list;list.Run(patched.code,menu,render,p);
    Check(list.options.size()==25 && list.requests==0 && list.closes==0 && list.stack.empty());
    for(size_t i=0;i<25;++i) Check(list.options[i]==std::pair<uint32_t,std::string>(0x7E00+uint32_t(i),std::string(kLabelKeys[i])));
    for(bool admit:{false,true}) {
      Vm select;select.admit=admit;select.Run(patched.code,accept,resume,p);
      Check(select.requests==1 && select.closes==unsigned(admit) && select.stack.empty());
    }
    std::cout<<"PASS Xbox menu state=1032 Quicksave="<<quicksave<<" code="<<raw.size()<<"->"<<patched.code.size()<<" 25 options; no request on listing; balanced selection/close paths\n";
  }
  std::cout<<"PASS "<<checks<<" private-input assertions; decoded SHA-256="<<digest<<"\n";
}
}
int main(int argc,char** argv) {
  try {if(argc!=2)throw std::runtime_error("Provide decoded spcellphonemain code; no game files are bundled");Verify(argv[1]);}
  catch(const std::exception& error) {std::cerr<<error.what()<<'\n';return 1;}
}
