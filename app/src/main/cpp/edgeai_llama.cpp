#include <jni.h>
#include <mutex>
#include <string>
#include <vector>

#if defined(EDGEAI_WITH_LLAMA_CPP)
#include "llama.h"
#endif

namespace {

std::mutex g_mutex;
std::string g_model_path;
bool g_initialized = false;

#if defined(EDGEAI_WITH_LLAMA_CPP)
llama_model * g_model = nullptr;
llama_context * g_ctx = nullptr;
llama_sampler * g_sampler = nullptr;
bool g_backend_initialized = false;
#endif

jstring JniThrow(JNIEnv * env, const char * message) {
    jclass ex = env->FindClass("java/lang/IllegalStateException");
    if (ex != nullptr) {
        env->ThrowNew(ex, message);
    }
    return nullptr;
}

std::string BuildPrompt(const std::string & prompt, const std::string & subject, const std::string & mode) {
    std::string full;
    full.reserve(prompt.size() + subject.size() + mode.size() + 160);
    full += "You are an offline tutor for Indian students.\n";
    full += "Subject: " + subject + "\n";
    full += "Mode: " + mode + "\n";
    full += "Answer clearly in 5-8 concise lines.\n";
    full += "Question: " + prompt + "\n";
    full += "Answer:";
    return full;
}

#if defined(EDGEAI_WITH_LLAMA_CPP)
void ReleaseRuntimeLocked() {
    if (g_sampler != nullptr) {
        llama_sampler_free(g_sampler);
        g_sampler = nullptr;
    }
    if (g_ctx != nullptr) {
        llama_free(g_ctx);
        g_ctx = nullptr;
    }
    if (g_model != nullptr) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
    g_initialized = false;
    g_model_path.clear();
}
#endif

} // namespace

extern "C" JNIEXPORT jboolean JNICALL
Java_com_edgeai_tutorlite_service_ai_runtime_LlamaCppRuntime_nativeInit(
        JNIEnv * env,
        jobject /* this */,
        jstring modelPath) {
    if (modelPath == nullptr) {
        JniThrow(env, "Model path is null.");
        return JNI_FALSE;
    }

    const char * modelChars = env->GetStringUTFChars(modelPath, nullptr);
    if (modelChars == nullptr) return JNI_FALSE;
    const std::string modelPathStr(modelChars);
    env->ReleaseStringUTFChars(modelPath, modelChars);

    std::lock_guard<std::mutex> lock(g_mutex);

#if defined(EDGEAI_WITH_LLAMA_CPP)
    if (!g_backend_initialized) {
        llama_backend_init();
        ggml_backend_load_all();
        g_backend_initialized = true;
    }

    if (g_initialized && g_model_path == modelPathStr) {
        return JNI_TRUE;
    }

    ReleaseRuntimeLocked();

    llama_model_params modelParams = llama_model_default_params();
    modelParams.n_gpu_layers = 0;

    g_model = llama_model_load_from_file(modelPathStr.c_str(), modelParams);
    if (g_model == nullptr) {
        JniThrow(env, "Failed to load GGUF model.");
        return JNI_FALSE;
    }

    llama_context_params ctxParams = llama_context_default_params();
    ctxParams.n_ctx = 2048;
    ctxParams.n_batch = 512;
    ctxParams.no_perf = true;

    g_ctx = llama_init_from_model(g_model, ctxParams);
    if (g_ctx == nullptr) {
        ReleaseRuntimeLocked();
        JniThrow(env, "Failed to initialize llama context.");
        return JNI_FALSE;
    }

    auto samplerParams = llama_sampler_chain_default_params();
    g_sampler = llama_sampler_chain_init(samplerParams);
    llama_sampler_chain_add(g_sampler, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(g_sampler, llama_sampler_init_top_p(0.9f, 1));
    llama_sampler_chain_add(g_sampler, llama_sampler_init_temp(0.7f));
    llama_sampler_chain_add(g_sampler, llama_sampler_init_dist(1234));

    g_model_path = modelPathStr;
    g_initialized = true;
    return JNI_TRUE;
#else
    (void) modelPathStr;
    JniThrow(env, "llama.cpp source backend is not configured for this build.");
    return JNI_FALSE;
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_edgeai_tutorlite_service_ai_runtime_LlamaCppRuntime_nativeRun(
        JNIEnv * env,
        jobject /* this */,
        jstring modelPath,
        jstring prompt,
        jstring subject,
        jstring mode,
        jstring imagePath) {
    (void) imagePath;
    if (modelPath == nullptr || prompt == nullptr || subject == nullptr || mode == nullptr) {
        return JniThrow(env, "Invalid input for inference.");
    }

    const char * modelChars = env->GetStringUTFChars(modelPath, nullptr);
    const char * promptChars = env->GetStringUTFChars(prompt, nullptr);
    const char * subjectChars = env->GetStringUTFChars(subject, nullptr);
    const char * modeChars = env->GetStringUTFChars(mode, nullptr);
    if (modelChars == nullptr || promptChars == nullptr || subjectChars == nullptr || modeChars == nullptr) {
        if (modelChars) env->ReleaseStringUTFChars(modelPath, modelChars);
        if (promptChars) env->ReleaseStringUTFChars(prompt, promptChars);
        if (subjectChars) env->ReleaseStringUTFChars(subject, subjectChars);
        if (modeChars) env->ReleaseStringUTFChars(mode, modeChars);
        return JniThrow(env, "Failed to read JNI strings.");
    }

    const std::string modelPathStr(modelChars);
    const std::string promptStr(promptChars);
    const std::string subjectStr(subjectChars);
    const std::string modeStr(modeChars);

    env->ReleaseStringUTFChars(modelPath, modelChars);
    env->ReleaseStringUTFChars(prompt, promptChars);
    env->ReleaseStringUTFChars(subject, subjectChars);
    env->ReleaseStringUTFChars(mode, modeChars);

    std::lock_guard<std::mutex> lock(g_mutex);

#if defined(EDGEAI_WITH_LLAMA_CPP)
    if (!g_initialized || g_ctx == nullptr || g_model == nullptr || g_sampler == nullptr) {
        return JniThrow(env, "Native llama runtime is not initialized.");
    }
    if (modelPathStr != g_model_path) {
        return JniThrow(env, "Model mismatch. Reinitialize runtime for this model.");
    }

    const llama_vocab * vocab = llama_model_get_vocab(g_model);
    std::string fullPrompt = BuildPrompt(promptStr, subjectStr, modeStr);

    const int nPromptTokens = -llama_tokenize(
            vocab,
            fullPrompt.c_str(),
            static_cast<int32_t>(fullPrompt.size()),
            nullptr,
            0,
            true,
            true
    );

    if (nPromptTokens <= 0) {
        return JniThrow(env, "Prompt tokenization failed.");
    }

    std::vector<llama_token> tokens(static_cast<size_t>(nPromptTokens));
    if (llama_tokenize(
            vocab,
            fullPrompt.c_str(),
            static_cast<int32_t>(fullPrompt.size()),
            tokens.data(),
            static_cast<int32_t>(tokens.size()),
            true,
            true
        ) < 0) {
        return JniThrow(env, "Tokenization failed.");
    }

    llama_batch batch = llama_batch_get_one(tokens.data(), static_cast<int32_t>(tokens.size()));
    if (llama_decode(g_ctx, batch) != 0) {
        return JniThrow(env, "Model decode failed.");
    }

    constexpr int kMaxNewTokens = 192;
    std::string output;
    output.reserve(1024);

    for (int i = 0; i < kMaxNewTokens; ++i) {
        llama_token next = llama_sampler_sample(g_sampler, g_ctx, -1);
        if (llama_vocab_is_eog(vocab, next)) break;

        char piece[256];
        int n = llama_token_to_piece(vocab, next, piece, sizeof(piece), 0, true);
        if (n > 0) {
            output.append(piece, piece + n);
        }

        batch = llama_batch_get_one(&next, 1);
        if (llama_decode(g_ctx, batch) != 0) {
            return JniThrow(env, "Decode loop failed.");
        }
    }

    if (output.empty()) {
        return JniThrow(env, "No output generated by model.");
    }

    return env->NewStringUTF(output.c_str());
#else
    (void) modelPathStr;
    (void) promptStr;
    (void) subjectStr;
    (void) modeStr;
    return JniThrow(env, "llama.cpp source backend is not configured for this build.");
#endif
}

extern "C" JNIEXPORT void JNICALL
Java_com_edgeai_tutorlite_service_ai_runtime_LlamaCppRuntime_nativeRelease(
        JNIEnv * /* env */,
        jobject /* this */) {
    std::lock_guard<std::mutex> lock(g_mutex);
#if defined(EDGEAI_WITH_LLAMA_CPP)
    ReleaseRuntimeLocked();
#else
    g_initialized = false;
    g_model_path.clear();
#endif
}
